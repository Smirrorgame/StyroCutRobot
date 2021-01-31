# StyroCutRobot
Ein Roboterarm (Adept Viper S850) soll ein konvexes 3D-Modell mittels Styroporschneidewerkzeug aus einem Styroporblock ausschneiden.
Außerdem soll ein weiterer Roboterarm genutzt werden, um den Styroporblock in Position zu halten und gegebenenfalls zu drehen.
Für die Kalibrierung der Roboter wird ein Polaris Trackingsystem genutzt:

## Kalibrierung: QR24-Algorithmus
Die Roboter müssen untereinander kalibriert werden, damit wir wissen, wo sie sich relativ zueinander befinden.
Wir nutzen zur Kalibrierung der Roboter den QR24-Algorithmus aus folgendem Paper von Prof. Ernst:<br>
[Non-orthogonal tool/flange and robot/world calibration](https://www.rob.uni-luebeck.de/~bruder/robprakt/Ernst,%20Richter,%20Matth%c3%a4us%20-%20Non-orthogonal%20tool-flange%20and%20robot-world%20calibration.pdf)

## Import in Eclipse
  1. In Eclipse mittels "Import existing Project into workspace" importieren.
  2. Rechtsklick auf das Projekt -> Properties
  3. Unter "Java Build Path" auf Source klicken
  4. "StyroCutRobot/src auswählen und rechts auf edit klicken.
  5. Im Dialogfenster auf "Finish" klicken, danach "Apply and Close"
  6. Main.java als Java Applikation starten

## Einbinden der Apache Commons Math Bibliothek
  1. Herunterladen der aktuellen Version (momentan 3.6.1) von [Apache Commons Math](http://commons.apache.org/proper/commons-math/download_math.cgi)
  2. Die Datei in einem beliebigen Ordner entpacken
  3. In Eclipse Rechtsklick auf das Projekt -> Properties
  4. Unter "Java Build Path" auf Libraries klicken
  5. "Modulepath" auswählen und rechts auf "Add External JARs" klicken.
  6. commons-math3-xxx.jar auswählen, wobei xxx die Versionsnummer ist, und auf "Apply and Close" klicken
  7. Eventuell im Package Explorer F5 für einen Refresh drücken, damit alles nochmal neu geladen wird
