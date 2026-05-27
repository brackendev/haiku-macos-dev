#!/usr/bin/env bb

(require '[babashka.process :refer [shell process]]
         '[clojure.java.io :as io]
         '[clojure.string :as str])

(def home (System/getenv "HOME"))

(def utm-dirs
  [(str home "/Library/Containers/com.utmapp.UTM/Data/Documents")
   (str home "/Library/Application Support/UTM")])

(defn find-utm-dir []
  (first (filter #(.isDirectory (io/file %)) utm-dirs)))

(defn plist-value [file key-path]
  (try
    (let [result (shell {:out :string :err :string :continue true}
                        "/usr/libexec/PlistBuddy" "-c" (str "Print :" key-path) file)]
      (when (zero? (:exit result))
        (str/trim (:out result))))
    (catch Exception _ nil)))

(defn list-vms [utm-dir]
  (->> (io/file utm-dir)
       (.listFiles)
       (filter #(str/ends-with? (.getName %) ".utm"))
       (filter #(.isDirectory %))
       (map (fn [vm-dir]
              (let [config (str (.getPath vm-dir) "/config.plist")
                    name (str/replace (.getName vm-dir) #"\.utm$" "")]
                {:name name
                 :path (.getPath vm-dir)
                 :config config
                 :arch (plist-value config "System:Architecture")
                 :mem (plist-value config "System:MemorySize")
                 :cpus (plist-value config "System:CPUCount")})))))

(defn parse-drives [config data-dir]
  (loop [idx 0
         drives []]
    (let [img-name (plist-value config (str "Drive:" idx ":ImageName"))
          img-type (plist-value config (str "Drive:" idx ":ImageType"))
          interface (plist-value config (str "Drive:" idx ":Interface"))
          readonly (plist-value config (str "Drive:" idx ":ReadOnly"))]
      (if (nil? img-type)
        drives
        (let [img-path (when img-name (str data-dir "/" img-name))
              drive (when (and img-path (.exists (io/file img-path)))
                      {:index idx
                       :path img-path
                       :type img-type
                       :interface interface
                       :readonly (= readonly "true")})]
          (recur (inc idx) (if drive (conj drives drive) drives)))))))

(defn build-drive-args [{:keys [index path type interface readonly]}]
  (let [base-opts (str "file=" path ",format=qcow2,aio=threads"
                       (when (= type "CD") ",media=cdrom")
                       (when readonly ",readonly=on"))]
    (case interface
      "IDE" ["-drive" (str "if=ide,index=" index "," base-opts)]
      "VirtIO" ["-drive" (str "if=virtio," base-opts)]
      "NVMe" ["-drive" (str "if=none,id=nvme" index "," base-opts)
              "-device" (str "nvme,drive=nvme" index ",serial=nvme" index)]
      ["-drive" base-opts])))

(defn build-qemu-cmd [vm]
  (let [config (:config vm)
        data-dir (str (:path vm) "/Data")
        arch (or (plist-value config "System:Architecture") "x86_64")
        target "pc" ; i440FX - q35 has USB keyboard issues at boot
        mem (or (plist-value config "System:MemorySize") "2048")
        cpus (or (plist-value config "System:CPUCount") "2")
        hypervisor (plist-value config "QEMU:Hypervisor")
        uefi (plist-value config "QEMU:UEFIBoot")
        display-hw (plist-value config "Display:0:Hardware")
        sound-hw (plist-value config "Sound:0:Hardware")
        net-hw (plist-value config "Network:0:Hardware")
        net-mac (or (plist-value config "Network:0:MacAddress") "52:54:00:12:34:56")
        rng (plist-value config "QEMU:RNGDevice")
        drives (parse-drives config data-dir)

        qemu-bin (str "qemu-system-" arch)

        host-arch (let [m (str/trim (:out (shell {:out :string} "uname" "-m")))]
                    (if (= m "arm64") "aarch64" m))

        accel (if (and (= hypervisor "true") (= arch host-arch))
                "hvf"
                "tcg,thread=multi,tb-size=1024")

        ;; Default to VGA: Haiku ships no vmware accelerant, so vmware-svga falls back to vesa.accelerant with no benefit.
        display-dev (case display-hw
                      "virtio-gpu-gl-pci" "virtio-gpu-pci"
                      "VGA")

        sound-args (case sound-hw
                     ("AC97" "intel-hda" "ich9-intel-hda")
                     ["-device" "AC97,audiodev=audio0"]
                     "usb-audio" ["-device" "usb-audio,audiodev=audio0"]
                     [])

        ;; hostfwd: 2222=SSH, 60780=HaikuClip, 60781=haiku-control daemon
        net-args (when net-hw
                   ["-netdev" "user,id=net0,hostfwd=tcp::2222-:22,hostfwd=tcp::60780-:60780,hostfwd=tcp::60781-:60781"
                    "-device" (str net-hw ",netdev=net0,mac=" net-mac)])

        rng-args (when (= rng "true")
                   ["-device" "virtio-rng-pci"])

        drive-args (mapcat build-drive-args drives)

        uefi-args (when (= uefi "true")
                    (let [fw-paths ["/opt/homebrew/share/qemu/edk2-x86_64-code.fd"
                                    "/usr/local/share/qemu/edk2-x86_64-code.fd"
                                    "/opt/homebrew/share/qemu/edk2-aarch64-code.fd"]
                          fw (first (filter #(.exists (io/file %)) fw-paths))]
                      (when fw ["-bios" fw])))]

    (vec (concat
          [qemu-bin
           "-machine" target
           "-accel" accel
           "-cpu" "max"
           "-smp" cpus
           "-m" mem
           ;; Reduce guest clock drift (doesn't fix MIDI timing but helps general time sync)
           "-rtc" "base=utc,clock=host,driftfix=slew"
           "-boot" "menu=on"
           "-device" display-dev
           "-display" "cocoa,zoom-to-fit=on"
           "-audiodev" "coreaudio,id=audio0"]
          sound-args
          net-args
          ["-device" "piix3-usb-uhci,id=uhci"
           "-device" "usb-tablet,bus=uhci.0"]
          rng-args
          drive-args
          uefi-args))))

(defn find-vm [utm-dir name]
  (first (filter #(= (:name %) name) (list-vms utm-dir))))

(defn print-vms [vms]
  (println "Available VMs:\n")
  (doseq [[idx vm] (map-indexed vector vms)]
    (printf "  %d) %s (arch: %s, RAM: %sMB, CPUs: %s)\n"
            (inc idx) (:name vm) (:arch vm) (:mem vm) (:cpus vm)))
  (println))

(defn show-cmd [vm]
  (println (str "QEMU command for: " (:name vm) "\n"))
  (println (str/join " " (build-qemu-cmd vm))))

(defn run-vm [vm]
  (println (str "Starting VM: " (:name vm) "\n"))
  (let [cmd (build-qemu-cmd vm)]
    (println "Command:")
    (println (str/join " " cmd))
    (println)
    (apply shell cmd)))

(defn check-qemu []
  (let [result (shell {:out :string :err :string :continue true} "which" "qemu-system-x86_64")]
    (when-not (zero? (:exit result))
      (println "QEMU not found. Install with:")
      (println "  brew install qemu")
      (System/exit 1))))

(defn print-usage []
  (println "UTM VM Runner (QEMU direct)\n")
  (println "Usage:")
  (println "  run-utm-vm.bb list           - List available VMs")
  (println "  run-utm-vm.bb run <vm-name>  - Run a VM")
  (println "  run-utm-vm.bb show <vm-name> - Show QEMU command")
  (println))

(defn -main [& args]
  (check-qemu)
  (let [utm-dir (find-utm-dir)]
    (when-not utm-dir
      (println "Error: No UTM VM directory found")
      (System/exit 1))

    (let [vms (list-vms utm-dir)
          [cmd vm-name] args]
      (case cmd
        "list" (print-vms vms)

        "run" (if-let [vm (find-vm utm-dir vm-name)]
                (run-vm vm)
                (do
                  (println (str "VM not found: " vm-name))
                  (print-vms vms)
                  (System/exit 1)))

        "show" (if-let [vm (find-vm utm-dir vm-name)]
                 (show-cmd vm)
                 (do
                   (println (str "VM not found: " vm-name))
                   (System/exit 1)))

        (do
          (print-usage)
          (print-vms vms))))))

(apply -main *command-line-args*)
