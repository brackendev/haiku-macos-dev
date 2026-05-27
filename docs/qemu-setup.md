# Running Haiku in QEMU on macOS

## Overview

This guide covers running Haiku in QEMU on macOS (Apple Silicon or Intel). It also links to the NFS setup for file sharing between macOS and Haiku.

## Prerequisites

- macOS (Apple Silicon or Intel)
- Homebrew
- QEMU (`brew install qemu`)
- Babashka (`brew install babashka`)
- UTM (https://mac.getutm.app) with a Haiku VM installed (download Haiku from https://www.haiku-os.org/get-haiku/)

## Creating the Haiku VM

If you don't already have a UTM VM with Haiku installed, do this once before using `run-utm-vm.bb`:

1. Install [UTM](https://mac.getutm.app) from the App Store or the direct download link.
2. Download a Haiku Anyboot image from [haiku-os.org/get-haiku](https://www.haiku-os.org/get-haiku/). The x86_64 release or nightly works.
3. In UTM, create a new VM: **Create a New Virtual Machine** > **Emulate** > **Other**.
4. Architecture: `x86_64`. Leave UTM's system type at its default. `run-utm-vm.bb` runs the VM with `-machine pc` (i440FX) regardless of the UTM setting.
5. Memory: 2048 MB or more. CPU cores: on Apple Silicon, use 1 core (TCG multicore is off by default in UTM and is unstable). On Intel, 2 or more cores is fine.
6. Storage: create a new disk image (16 GB is plenty).
7. Attach the Haiku ISO when prompted, or add it as a CD/DVD drive afterward.
8. Save and start the VM. At the Haiku boot screen, choose **Install Haiku** (or **Try Haiku** if you want to look around first; you can launch Installer from the Deskbar later).
9. The Installer will refuse to use a blank virtual disk. From the Installer, open **Setup partitions...** to launch DriveSetup, then on the new disk:
   - **Partition** > **Intel** > **Initialize** to create an Intel partition map.
   - Select the unused space, **Create** > **Be File System** partition (mark it active).
   - **Partitions** > **Format** > **Be File System** to format it.
   - Quit DriveSetup and select the new partition as the install destination in Installer.
10. When installation completes, remove the ISO from the VM's drive list and reboot.

Quit UTM after the VM is working. After this one-time setup, run the VM with `run-utm-vm.bb run <name>`.

## Running UTM VMs with QEMU

A Babashka script (`run-utm-vm.bb`) reads UTM VM configurations and launches them with QEMU.

### Script Location

```
~/bin/run-utm-vm.bb
```

### Usage

```bash
# List available VMs
run-utm-vm.bb list

# Run a VM
run-utm-vm.bb run Haiku

# Show QEMU command without running
run-utm-vm.bb show Haiku
```

### Notes

- The script searches `~/Library/Containers/com.utmapp.UTM/Data/Documents/` and `~/Library/Application Support/UTM`
- Quit UTM before running VMs with QEMU because UTM locks disk images while it is using them
- x86_64 VMs use TCG on Apple Silicon hosts (cross-architecture emulation, slower) and HVF on Intel hosts (native virtualization)

## Display Scaling on Retina Displays

The script launches QEMU with `-display cocoa,zoom-to-fit=on`. On Retina Macs, each guest pixel maps to one physical pixel by default, so Haiku can look very small. With `zoom-to-fit=on`, resizing the QEMU window scales the guest display. Lower the guest resolution in Haiku's Screen preferences, then enlarge the QEMU window.

## NFS File Sharing

See [nfs.md](nfs.md) for NFS exports, Haiku mounting, boot-time mounting, and troubleshooting.

## QEMU Networking

QEMU user-mode networking (`-netdev user`) provides these addresses:

| Address | Purpose |
|---------|---------|
| 10.0.2.2 | Host (macOS) |
| 10.0.2.3 | DNS server |
| 10.0.2.15 | Guest (typical) |

Haiku can reach macOS at `10.0.2.2`.

### Port Forwarding

The `run-utm-vm.bb` script forwards these ports:

| Host Port | Guest Port | Service |
|-----------|------------|---------|
| 2222 | 22 | SSH |
| 60780 | 60780 | HaikuClip |
| 60781 | 60781 | haiku-control-mcp |

## Troubleshooting

### Disk image locked

- Quit UTM before running VMs with QEMU
- Check for other QEMU processes: `ps aux | grep qemu`

### No network in guest

- Verify the VM has a network device configured in UTM
- Check `ping 10.0.2.2` from Haiku

### NFS issues

See [nfs.md](nfs.md#troubleshooting) for NFS troubleshooting and other file sharing methods.

### UI elements appear too small

The QEMU window renders at full Retina pixel density. Lowering the Haiku resolution alone can make the window shrink instead of making the interface larger. Resize the QEMU window after lowering the guest resolution.

## References

- [Haiku NFS Discussion](https://discuss.haiku-os.org/t/mount-nfs-under-haiku/10655)
- [QEMU User Networking](https://wiki.qemu.org/Documentation/Networking#User_Networking_(SLIRP))
