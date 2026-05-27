# SSH on Haiku

## Setup

On the Haiku machine:

1. Set a password for the user account:
   ```bash
   passwd
   ```

2. Edit `/boot/system/settings/ssh/sshd_config`:
   ```
   PasswordAuthentication yes
   PermitRootLogin yes
   PubkeyAuthentication yes
   AuthorizedKeysFile .ssh/authorized_keys
   ```

   Haiku's standard `user` account has uid 0, so OpenSSH treats it as root. Without `PermitRootLogin yes`, OpenSSH rejects password login for `user`.

3. Start or restart sshd:
   ```bash
   kill sshd
   /boot/system/bin/sshd
   ```

## Connecting from Mac

### Direct Network (Physical Machine or Bridged VM)

Connect to the Haiku machine's IP address:

```bash
ssh user@<haiku-ip>
```

### QEMU with User-Mode Networking

QEMU user-mode networking uses port forwarding. The launcher forwards host port 2222 to guest port 22:

```bash
ssh -p 2222 user@localhost
```

If the connection is refused, verify QEMU was started with this port forward:
```
-netdev user,id=net0,hostfwd=tcp::2222-:22
```

The `run-utm-vm.bb` script includes this forward.

## Key-Based Authentication

Use key authentication for scripts and repeated connections.

1. Generate a key:
   ```bash
   ssh-keygen -t ed25519 -f ~/.ssh/haiku -N "" -C "haiku"
   ```

2. Copy to Haiku:

   **Direct network:**
   ```bash
   ssh-copy-id -o PreferredAuthentications=password -i ~/.ssh/haiku user@<haiku-ip>
   ```

   **QEMU:**
   ```bash
   ssh-copy-id -p 2222 -o PreferredAuthentications=password -i ~/.ssh/haiku user@localhost
   ```

3. Connect:

   **Direct network:**
   ```bash
   ssh -i ~/.ssh/haiku user@<haiku-ip>
   ```

   **QEMU:**
   ```bash
   ssh -p 2222 -i ~/.ssh/haiku user@localhost
   ```

### SSH Configuration

Add this to `~/.ssh/config`. `IdentitiesOnly yes` prevents SSH from trying every key in `~/.ssh/`, which can cause "Too many authentication failures" errors.

```
# Direct network Haiku
Host haiku
    HostName <haiku-ip>
    User user
    IdentityFile ~/.ssh/haiku
    IdentitiesOnly yes

# QEMU Haiku
Host haiku-qemu
    HostName localhost
    Port 2222
    User user
    IdentityFile ~/.ssh/haiku
    IdentitiesOnly yes
```

Then connect with:
```bash
ssh haiku
ssh haiku-qemu
```

## SSH Tunneling

Forward ports through SSH for services such as VNC or web servers.

### Direct Network

```bash
# VNC (port 5900)
ssh -L 5900:localhost:5900 user@<haiku-ip>

# Any port
ssh -L <local-port>:localhost:<remote-port> user@<haiku-ip>

# Background tunnel (no shell)
ssh -fN -L 5900:localhost:5900 user@<haiku-ip>
```

### QEMU

```bash
ssh -p 2222 -L 5900:localhost:5900 user@localhost
```

## Troubleshooting

### Connection Refused

**Direct network:** Verify that sshd is running on Haiku:
```bash
ps | grep sshd
netstat -n | grep 22
```

If not running, start it:
```bash
/boot/system/bin/sshd
```

**QEMU:** Verify the port forward in the QEMU command.

### "Bad configuration option: permitrootlogin"

`PermitRootLogin` is in the wrong file. It belongs in `sshd_config`, not `ssh_config`.

**Fix:** Remove the line from `/boot/system/settings/ssh/ssh_config`.

### "Too many authentication failures"

The SSH client is trying too many keys before password authentication.

**Fix:** Force password authentication:
```bash
ssh -o PreferredAuthentications=password user@<haiku-ip>
ssh-copy-id -o PreferredAuthentications=password -i ~/.ssh/haiku user@<haiku-ip>
```

### Test Local SSH First

From within Haiku:
```bash
ssh user@127.0.0.1
```

If this works but a remote connection fails:
- **Direct network:** Check firewall and network connectivity
- **QEMU:** Check the port forward

### Shell Doesn't Exit After Remote Commands

Haiku's shell does not exit after commands complete over SSH. Add `exit`:

```bash
# Hangs indefinitely
ssh user@<haiku-ip> 'echo hello'

# Exits after command
ssh user@<haiku-ip> 'echo hello; exit'

# Preserve exit code
ssh user@<haiku-ip> 'make build; exit $?'
```

### Recommended SSH Options for Haiku

```bash
ssh -n -o ServerAliveInterval=15 -o ServerAliveCountMax=3 user@<haiku-ip>
```

- `-n`: Redirect stdin from `/dev/null`
- `-o ServerAliveInterval=15`: Check whether the connection is alive
- `-o ServerAliveCountMax=3`: Close the connection after three missed checks
