# Haiku on macOS

Documentation and tools for running Haiku® in QEMU on macOS.

The launcher is a Babashka script that reads existing UTM VM configurations and runs them with QEMU. You can keep using UTM to edit VMs and use this script to run them from the command line. The documentation also covers SSH, NFS file sharing, Deskbar replicant development, VPN experiments, and BeOS icon design.

## Prerequisites

- macOS (Apple Silicon or Intel)
- Homebrew
- QEMU (`brew install qemu`)
- Babashka (`brew install babashka`)
- UTM (https://mac.getutm.app) with a Haiku VM installed (download Haiku from https://www.haiku-os.org/get-haiku/)

## Documentation

| Topic | File |
|-------|------|
| QEMU setup, VM script, networking | [qemu-setup.md](docs/qemu-setup.md) |
| NFS file sharing, boot-time mounting | [nfs.md](docs/nfs.md) |
| SSH setup, key authentication | [ssh.md](docs/ssh.md) |
| VPN (OpenVPN) | [vpn.md](docs/vpn.md) |
| Deskbar replicant development | [deskbar.md](docs/deskbar.md) |
| BeOS icon design guidelines | [beos-icon-style.md](docs/beos-icon-style.md) |

## VM Management

A Babashka script reads UTM VM configurations and launches them with QEMU.

```bash
run-utm-vm.bb list        # List available VMs
run-utm-vm.bb run Haiku   # Run a VM
run-utm-vm.bb show Haiku  # Show QEMU command
```

Notes:
- The script searches `~/Library/Containers/com.utmapp.UTM/Data/Documents/` and `~/Library/Application Support/UTM`
- Quit UTM before running VMs with QEMU because UTM locks disk images while it is using them
- x86_64 VMs use TCG on Apple Silicon hosts (cross-architecture emulation) and HVF on Intel hosts (native virtualization)

## QEMU Networking

| Address | Purpose |
|---------|---------|
| 10.0.2.2 | Host (macOS) |
| 10.0.2.3 | DNS server |
| 10.0.2.15 | Guest (typical) |

## Haiku Shell Configuration

| File | Purpose |
|------|---------|
| `~/config/settings/profile` | Shell initialization (equivalent to `.bashrc`) |
| `~/config/settings/boot/UserSetupEnvironment` | Environment variables set before desktop starts |
| `~/config/settings/boot/UserBootscript` | Script run at boot (see [nfs.md](docs/nfs.md)) |
| `~/bin` | User scripts (add to PATH in profile) |

## Related Projects

| Project | Description |
|---------|-------------|
| [HaikuClip](https://github.com/brackendev/HaikuClip) | Clipboard sync between Haiku and macOS |
| [haiku-control-mcp](https://github.com/brackendev/haiku-control-mcp) | MCP server for controlling Haiku |
| [haiku-lisp-dev](https://github.com/brackendev/haiku-lisp-dev) | Lisp GUI development on Haiku |

## Trademarks

Haiku® and the HAIKU logo® are registered trademarks of [Haiku, Inc.](http://www.haiku-inc.org) and are developed by the [Haiku Project](http://www.haiku-os.org).

Mac and macOS are trademarks of Apple Inc., registered in the U.S. and other countries and regions.

haiku-macos-dev is an independent project. It is not affiliated with, endorsed, or sponsored by Haiku, Inc., the Haiku Project, or Apple Inc.

## License

[MIT](LICENSE)
