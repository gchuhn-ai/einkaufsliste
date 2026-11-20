# Einkaufsliste (Schritt 1)

Android-App mit Liste, Abhaken, Löschen und Spracheingabe (Deutsch).
Mehrere Artikel auf einmal: "Milch und Eier, Brot" ergibt drei Einträge.

## APK bauen – Weg A: Android Studio
1. Android Studio installieren (developer.android.com/studio).
2. Ordner `einkaufsliste` öffnen, warten bis der Gradle-Sync fertig ist.
3. Handy per USB anschließen (Entwickleroptionen + USB-Debugging an) und auf "Run" klicken.
   Oder: Menü Build > Build APK(s), die Datei liegt dann in `app/build/outputs/apk/debug/`.

## APK bauen – Weg B: GitHub (ohne Installation)
1. Kostenloses GitHub-Konto, neues privates Repository anlegen.
2. Den Inhalt dieses Ordners hochladen (inkl. `.github`).
3. Reiter "Actions" > "APK bauen" > "Run workflow". Nach ein paar Minuten gibt es unter
   dem fertigen Lauf den Download "einkaufsliste-apk".
4. APK aufs Handy übertragen, antippen und "Aus dieser Quelle installieren" erlauben.
