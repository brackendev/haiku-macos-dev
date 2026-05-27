# VPN on Haiku

**Status: not functional.** OpenVPN connects and creates the tunnel, but traffic does not route through it. OpenVPN expects point-to-point support for TUN, and Haiku's network stack does not implement that support. The [Google Summer of Code 2023 VPN project](https://discuss.haiku-os.org/t/gsoc-2023-vpn-support-project-update-5-haiku-project/13787) hit the same limit and switched to TAP mode, which most commercial VPN providers do not support.

The setup below is reference material for experiments and fixes.

## Setup

1. Install the script:

```bash
mkdir -p ~/bin
cp ~/mac/haiku-macos-dev/haiku/scripts/vpn-connect ~/bin/
chmod +x ~/bin/vpn-connect
```

2. Add `~/bin` to PATH in `~/config/settings/profile`:

```bash
echo 'export PATH="$HOME/bin:$PATH"' >> ~/config/settings/profile
source ~/config/settings/profile
```

3. Create the VPN configuration directory:

```bash
mkdir -p ~/.vpn
chmod 700 ~/.vpn
```

4. Add your OpenVPN configuration:

```bash
cp /path/to/your-config.ovpn ~/.vpn/config.ovpn
```

5. Create credentials file:

```bash
echo "your-username" > ~/.vpn/auth
echo "your-password" >> ~/.vpn/auth
chmod 600 ~/.vpn/auth
```

## Usage

```bash
# Show help
vpn-connect

# Connect without routing traffic through the VPN
vpn-connect -x

# Attempt to route all traffic through the VPN
vpn-connect -r

# Attempt to route specific networks through the VPN
vpn-connect -R 10.0.0.0/8,192.168.0.0/16

# Use a different configuration
vpn-connect -x -c ~/.vpn/work.ovpn -a ~/.vpn/work-auth
```

## Options

| Option | Description |
|--------|-------------|
| `-x, --connect` | Connect to the VPN without adding routes |
| `-c, --config FILE` | OpenVPN configuration file (default: `~/.vpn/config.ovpn`) |
| `-a, --auth FILE` | Credentials file (default: `~/.vpn/auth`) |
| `-r, --route-all` | Attempt to route all traffic through the VPN |
| `-R, --routes CIDRS` | Attempt to route specific networks through the VPN |
| `-d, --daemon` | Run in the background |

## How It Works

1. Checks for OpenVPN and installs it if needed
2. Creates the `tun/0` interface
3. Connects to the VPN server
4. Adds routes based on options

OpenVPN on Haiku does not manage routes, so the script tries to add them.

## Configuration File Compatibility

If OpenVPN reports `CRL not loaded`, remove the `<crl-verify>...</crl-verify>` section from your `.ovpn` file.

## Troubleshooting

### TLS handshake failed

Check the `<ca>` certificate in your configuration.

### Routes not working

Check the route table:

```bash
route list
```

Verify the tunnel has an IP:

```bash
ifconfig tun/0
```

### High CPU usage

This is a known issue. OpenVPN on Haiku uses high CPU because Haiku lacks asynchronous I/O for the TUN driver.

## Disconnect

The script prints the OpenVPN process ID after connection. To disconnect:

```bash
kill <pid>
```

Or find and kill the process:

```bash
ps | grep openvpn
kill <pid>
```

## What Would Fix It

1. **Kernel work**: Add point-to-point support to Haiku's network stack
2. **TAP mode**: Use TAP instead of TUN. This requires VPN server support, and most commercial VPN providers do not offer it.

## Other Limitations

- There is no graphical client.
- High CPU usage occurs because of asynchronous I/O limitations.
- Configurations with `CRL not loaded` errors need the CRL section removed.
