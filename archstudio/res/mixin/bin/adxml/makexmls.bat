java c2demo.nad.Description > ad.xml
java c2demo.nad.Description -stealth > ad-stealth.xml
java c2demo.nad.Description -2displays > ad-2displays.xml
java c2demo.nad.Description -monitors > ad-monitors.xml
java c2demo.nad.Description -radarcontrol > ad-radarcontrol.xml
java c2demo.nad.Description -replacements > ad-replacements.xml

java archstudio.comp.archdiff.ArchDiff ad.xml ad-stealth.xml > diff-stealth.xml
java archstudio.comp.archdiff.ArchDiff ad.xml ad-2displays.xml > diff-2displays.xml
java archstudio.comp.archdiff.ArchDiff ad.xml ad-monitors.xml > diff-monitors.xml
java archstudio.comp.archdiff.ArchDiff ad.xml ad-radarcontrol.xml > diff-radarcontrol.xml
java archstudio.comp.archdiff.ArchDiff ad.xml ad-replacements.xml > diff-replacements.xml
