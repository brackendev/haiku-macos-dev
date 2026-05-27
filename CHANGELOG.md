# Changelog

This file tracks user-facing changes.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.0] - 2026-05-27

### Added

#### VM Launcher
- Babashka script (`run-utm-vm.bb`) that reads UTM VM configurations and runs them with QEMU
- `list` command showing available VMs with architecture, memory, and CPU details
- `run` command to start a VM directly from the command line
- `show` command to print the QEMU command without executing it
- Automatic host architecture detection for HVF (native) on Intel or TCG (emulation) on Apple Silicon
- UTM plist parsing for system settings, drives, display, sound, and network configuration
- Drive support for IDE, VirtIO, and NVMe interfaces with qcow2 format
- UEFI boot support with automatic firmware path detection (Homebrew on Apple Silicon and Intel)
- Cocoa display with zoom-to-fit for Retina scaling
- CoreAudio audio output with AC97 and USB audio device support
- USB tablet device for seamless mouse integration
- VirtIO random number generator support
- Clock drift reduction with host-synced RTC
- Port forwarding: 2222 to SSH, 60780 to HaikuClip, 60781 to haiku-control daemon

#### NFS File Sharing
- macOS NFS export configuration with firmlink-aware paths (`/System/Volumes/Data/Users/...`)
- Haiku `mount_nfs` instructions with uid and gid mapping
- Boot script (`haiku/scripts/UserBootscript`) that auto-mounts the NFS share with retry logic for cold-boot RPC delays
- Documentation of export options (`-mapall`, `-alldirs`, `-insecure`) and network exposure considerations
- Alternative file sharing methods: rsync over SSH, HTTP server, shared disk image, SMB

#### SSH
- SSH setup guide for Haiku including sshd configuration and key-based authentication
- SSH config examples for direct network and QEMU port-forwarded connections
- SSH tunneling instructions for forwarding arbitrary ports
- Workaround documentation for Haiku's shell not exiting after remote commands (`; exit` suffix)
- Troubleshooting for connection refused, too many authentication failures, and PermitRootLogin placement

#### VPN (Experimental)
- OpenVPN connection script (`haiku/scripts/vpn-connect`) with automatic dependency installation
- Options for no routing (`-x`), route all traffic (`-r`), or route specific networks (`-R`)
- Daemon mode (`-d`) for background operation
- Configurable OpenVPN configuration and credentials file paths
- Documentation of Haiku TUN limitations that prevent traffic routing through the tunnel

#### Deskbar Development
- Replicant caching documentation and cache-clearing procedure
- Troubleshooting for hanging `desklink --list`, missing tray icons, and unresponsive Deskbar
- Process recovery instructions using Haiku's `ps` column layout

#### BeOS Icon Style Guide
- Reference guide for the BeOS icon aesthetic: 3/4 isometric perspective, upper-left lighting, hand-painted gradients
- Practical construction rules for pixel-first icon design at 32x32 and 16x16
- Color, shading, outline, and metaphor guidelines
