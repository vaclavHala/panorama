#!/usr/bin/env python
from __future__ import print_function
import urllib
import urllib2
import xml.etree.ElementTree
import sys
import argparse


def eprint(*args, **kwargs):
    print(*args, file=sys.stderr, **kwargs)


parser = argparse.ArgumentParser()
parser.add_argument('lon', type=float)
parser.add_argument('lat', type=float)
parser.add_argument('--width', type=float, default=1.0)
parser.add_argument('--height', type=float, default=1.0)
parser.add_argument('--cached', type=file)

args = parser.parse_args()
eprint(args)

BASE_URL = 'http://overpass-api.de/api/interpreter'

if args.cached is not None:
    features_xml = args.cached.read()
else:
    req = "data=node(%s, %s, %s, %s)[natural=peak];out;" % (args.lat, args.lon, args.lat + args.height, args.lon + args.width)
    req_enc = req.replace(" ", "%20")

    req_full = BASE_URL + "?" + req_enc

    eprint(BASE_URL + "?" + req)
    eprint(req_full)

    features_xml = urllib2.urlopen(req_full).read()
    with open("tmp.xml", 'w') as tmp_out:
        tmp_out.write(features_xml)

e = xml.etree.ElementTree.fromstring(features_xml)

fs = []

for f in e.findall('node'):
    name_tag = f.find("tag[@k='name']")
    if name_tag is None or name_tag.get('v') is None:
        eprint("Skipping:\n" + xml.etree.ElementTree.tostring(f))
        continue
    name = name_tag.get('v')
    lon = float(f.get('lon'))
    lat = float(f.get('lat'))
    eprint(name, lon, lat)
    fs.append((name, lon, lat))

# sorted top to bottom then left to right
for (name, lon, lat) in sorted(fs, key=lambda (name, lon, lat): (lat, -lon)):
    print(u"{},{},{}".format(lat, lon, name).encode("utf-8"))
