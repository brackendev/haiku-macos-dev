# NFS File Sharing

NFS shares files between macOS and the Haiku VM.

## macOS Setup

1. Check your macOS user and group IDs:

```bash
id -u
id -g
```

The examples below use `501:20`, which is common for the first macOS user account. Replace those values if your `id` output differs.

2. Create the exports file:

```bash
sudo bash -c 'echo "/System/Volumes/Data/Users/YOUR_USERNAME/Dev -mapall=501:20 -alldirs -insecure" > /etc/exports'
```

3. Start or restart NFS:

```bash
sudo nfsd enable
sudo nfsd restart
```

4. Verify the export:

```bash
showmount -e localhost
```

### Export Options

| Option | Purpose |
|--------|---------|
| `-mapall=501:20` | Map all access to macOS uid 501 and gid 20 |
| `-alldirs` | Allow mounting subdirectories |
| `-insecure` | Allow connections from unprivileged ports (required for QEMU) |

### Network Exposure

The export has no network restriction. QEMU user-mode networking does not give the NFS server a stable source IP for the guest, and restricting the export to `10.0.2.0/24` causes mount failures.

macOS `nfsd` listens on all interfaces (`*.2049` on tcp/udp), so hosts on the same local network can reach the export. `-mapall=501:20` maps identity. It does not restrict access. Use this on a trusted network or restrict access with a host firewall.

### macOS Path (Firmlinks)

macOS uses firmlinks. Export `/System/Volumes/Data/Users/...`, not `/Users/...`.

## Haiku Setup

1. Create mount point:

```bash
mkdir -p ~/mac
```

2. Mount the NFS share. Replace `501 20` if your macOS user and group IDs differ:

```bash
mount_nfs 10.0.2.2:/System/Volumes/Data/Users/YOUR_USERNAME/Dev/Haiku ~/mac 501 20
```

3. Optional: add an alias to your profile:

```bash
echo 'alias mountdev="mount_nfs 10.0.2.2:/System/Volumes/Data/Users/YOUR_USERNAME/Dev/Haiku ~/mac 501 20"' >> ~/config/settings/profile
```

## Auto-Mount on Boot

Copy the boot script from this repository:

```bash
cp ~/mac/haiku-macos-dev/haiku/scripts/UserBootscript ~/config/settings/boot/
chmod +x ~/config/settings/boot/UserBootscript
```

If NFS is not mounted yet, copy manually from `haiku/scripts/UserBootscript`.

Edit the copied script and replace `YOUR_USERNAME` in `NFS_SRC`. The mount fails until that placeholder is replaced.

The script retries `mount_nfs` because RPC services may still be starting during boot.

Notes:
- `UID` is readonly in bash, so the script uses `NFS_UID` and `NFS_GID`
- Haiku's `mount` without arguments shows usage, not mounts. Use `df` to check.

## Troubleshooting

### "No such file or directory" on mount

- Verify `~/mac` exists in Haiku
- Check that the export path uses `/System/Volumes/Data/Users/...`
- Use `mount_nfs`, not `mount -t nfs`

### NFS export not working

```bash
# Check NFS is running
nfsd status

# Check exports
showmount -e localhost

# Restart NFS
sudo nfsd restart
```

## Alternative Methods

If NFS does not work:

| Method | Pros | Cons |
|--------|------|------|
| rsync over SSH | No host daemon, works over the existing SSH forward | Not live; each sync must be run manually |
| HTTP server | Simple, no setup in guest | Download only |
| Shared disk image | Works without network setup | Must unmount to sync |
| SMB | GUI support in Haiku | Protocol mismatch issues |

### rsync over SSH

Install rsync on Haiku with HaikuDepot or `pkgman install rsync`. Sync from macOS to Haiku through the SSH forward on port 2222:

```bash
rsync -avz -e 'ssh -p 2222 -i ~/.ssh/haiku -o IdentitiesOnly=yes' \
  ~/Dev/Haiku/ user@localhost:~/mac/
```

Reverse the source and destination to copy files back to macOS. Each sync is independent, so a VM reboot or network failure does not leave a stale mount.

The `-a` flag preserves numeric uid and gid. Files arrive on Haiku owned by the macOS uid, often `501`, rather than the SSH login user. This matches the NFS export's `-mapall=501:20` behavior. Use `-rtvz` instead of `-avz` if you want files owned by the SSH login user.

### HTTP Server (Download Only)

On Mac:
```bash
cd ~/Dev && python3 -m http.server 8080
```

In Haiku, open WebPositive to `http://10.0.2.2:8080`

## References

- [Haiku NFS Discussion](https://discuss.haiku-os.org/t/mount-nfs-under-haiku/10655)
