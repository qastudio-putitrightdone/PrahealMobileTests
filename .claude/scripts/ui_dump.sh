#!/bin/bash
set -e
OUT=${1:-/tmp/praheal_ui.xml}
DEVICE=${ANDROID_SERIAL:-$(adb devices | awk 'NR>1 && $2=="device" {print $1; exit}')}
[ -z "$DEVICE" ] && { echo "No adb device connected" >&2; exit 1; }
rm -f "$OUT"
adb -s "$DEVICE" shell uiautomator dump /sdcard/praheal_ui.xml >/dev/null
adb -s "$DEVICE" pull /sdcard/praheal_ui.xml "$OUT" >/dev/null
python3 - "$OUT" <<'PY'
import sys, xml.etree.ElementTree as ET
for n in ET.parse(sys.argv[1]).iter('node'):
    a = n.attrib
    if a.get('text') or a.get('content-desc') or a.get('clickable') == 'true' or 'EditText' in a['class']:
        desc = a.get('content-desc', '').encode('unicode_escape').decode()
        flags = ' '.join(f for f, on in [('clickable', a.get('clickable') == 'true'), ('password', a.get('password') == 'true')] if on)
        print(f"{a['class'].split('.')[-1]:<10} text={a.get('text')!r} hint={a.get('hint')!r} desc='{desc}' {flags} {a['bounds']}")
PY
