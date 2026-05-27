# Deskbar Development Tips

## Replicant Caching

Deskbar caches loaded add-ons in `~/config/settings/deskbar/replicants`. After changing an add-on:

1. Stop the daemon
2. Delete `~/config/settings/deskbar/replicants`
3. Restart Deskbar or reboot

Without clearing the cache, Deskbar keeps using the old add-on binary.

## Troubleshooting

### desklink --list hangs

If `desklink --list` hangs, Deskbar is likely trying to load a broken add-on. Reboot and clear the replicants cache before the add-on loads again.

### Icon not appearing

If the tray icon does not appear after starting the daemon:

1. Check that the add-on file exists at the expected location
2. Verify the add-on exports `instantiate_deskbar_item()`
3. Clear the replicants cache and restart Deskbar

### Unresponsive Deskbar

A Deskbar add-on that deadlocks the message loop makes the entire Deskbar unresponsive (`desklink --list` hangs, tray stops updating). Recovery requires killing processes manually.

Haiku does not include `pkill` or `pgrep`. Use `ps | grep | awk` to find process IDs. The `ps` column layout on Haiku: column 1 is the team path, column 2 is the team ID (PID), column 3 is the thread count.

```bash
# Kill the offending add-on first (example: HaikuClip)
kill -9 $(ps | grep HaikuClip | grep -v grep | awk '{print $2}')

# Kill Deskbar
kill -9 $(ps | grep Deskbar | grep -v grep | awk '{print $2}')
```

Deskbar restarts automatically after being terminated. There is no need to reboot or manually restart it.
