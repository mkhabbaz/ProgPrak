package client.engine;

//diverse imports vom Package shared
import datenstruktur.Spieler;
import gui.HindiBones;
import gui.Spielflaeche;
import shared.nachrichten.*;
import datenstruktur.Heiltrank;
import datenstruktur.*;
import gui.*;

/**
 * Klasse ClientEngine: Dies ist die Zentrale Klasse der clientseitigen Kommunikation
 * 
 * @author Pilz, Konstantin, 5957451
 *
 */

/*
*
* itemID = 0: Heiltrank
* itemID = 1: Schluessel
*
* */

public class ClientEngine extends Thread {

	HindiBones fenster = new HindiBones(16, 16, "Level1");
	public Spielflaeche spielflaeche = new Spielflaeche(fenster);
	public Spieler spieler; //import aus shared.spieler, welcher alle Merkmale des Charakters beinhaltet
		String benutzername, passwort;
	//import von der gui; hier laeuft das Fenster des Spiels plus die Hauptschnittstelle des Clients drüber
	public boolean eingeloggt = false;
	public boolean login = false;
	public boolean neuesLevel = false;

	int levelzaehler = 0;

	TestumgebungClientEngine testInstanz = new TestumgebungClientEngine();

	/**
	 * Methode nachrichtentypZuordnen: Verarbeitet die vom Server an Client versendete Nachricht
	 * je nach enthaltenem Nachrichtentyp
	 *
	 * @author Pilz, Konstantin, 5957451
	 *
	 */

	/*public void nachrichtentypZuordnen(String empfangenerString) {

		switch (empfangenerString) {             //Switch-Case Anweisung wird zur Unterscheidung eingehender Nachrichten verwendet
			case 0:
				//LOGIN
				if (eingeloggt = true) {
					LoginAntwort daten = new LoginAntwort(true) ; //eingehendeNachricht
					//Level, Levelzaehler ist noch nicht vorhanden
					spielflaeche.level = daten.karte;
					spielflaeche.levelzaehler = daten.levelzaehler;
					spieler.setName(daten.name);

					//spieler.setPasswort(daten.passwort);
					this.eingeloggt = daten.eingeloggt;

					testInstanz.serverAntwort(4, "Login wurde erfolgreich empfangen");
					this.login = true;
				}
				break;
			case 1://anmelden
				Paket serverAntwort = sende(login);
				nachrichtentypZuordnen(serverAntwort);
				return serverAntwort.getNachricht().gueltig; //gueltig muss noch unter shared erstellt werden, dass Daten gueltig ist
				systemnachricht("Position des Spielers: " + empfangeneNachricht.getXPos() + ", " + empfangeneNachricht.getYPos());
				break;
			case 2:
				//
				testInstanz.serverAntwort("Trank an " + empfangeneNachricht.getXPos() + ", " + empfangeneNachricht.getYPos()
						+ " wurde aufgenommen");
				break;
			case 3:
				//
				testInstanz.serverAntwort("Schluessel an" + empfangeneNachricht.getXPos() + ", " + empfangeneNachricht.getYPos()
						+ " wurde aufgenommen");
				break;
			case 4:
				//
				testInstanz.serverAntwort("Das Level wurde abgeschlossen!");

				break;
			case 5:
				//
				testInstanz.serverAntwort(empfangeneNachricht.fehler);
				break;
			case 6:
				verarbeiteCheat(empfangenerString);
				break;
			case 7: {
				for (int i = 0; i < empfangeneNachricht.leveldaten.length; i++) {
					alleLevel[i] = new Level(i, empfangeneNachricht.leveldaten[i]);
				}
			}
		}
	}*/

/**
 * Methode verarbeiteCheat: Verarbeitet von Server empfangene Cheat-Nachricht (führt aus)
 *
 * @author Pilz, Konstantin, 5957451
 * @param cheat: Zu verarbeitende Nachricht, welche dem Spiele Vorteile bringen sollen
 *
 */
	public boolean verarbeiteCheat(String cheat){

		if (cheat == "<#godmode") {
			testInstanz.serverAntwort(0);
			return true;
		}
		if (cheat == "<#nebelWeg") {
			testInstanz.serverAntwort(1);
			return true;
		}
		else {
			System.out.println("Error! Ungültiger Cheat!");
			return false;
		}
	}

    /**
     * Methode nimmHeiltrank: In dieser Methode kann der Spieler an einer bestimmten Position einen Trank aufnehmen.
	 * Nachricht an Server bestaetigt, ob an dieser Stelle an Item liegt und welche Art Item und aendert nach Aufnahme
	 * den Status dieses Feldes.
	 * @author Pilz, Konstantin, 5957451
     */
	public void nimmHeiltrank(){
		spieler = fenster.spieler;
		Heiltrank heilgetraenk = new Heiltrank(20);
		spieler.nimmHeiltrank(heilgetraenk);

		int posX = spieler.getXPos();
		int posY = spieler.getYPos();
		int itemIDlocal = 0;

		testInstanz.serverAntwort(posX, posY, itemIDlocal);
	}

	/**
     * Methode aufnehmenSchluessel: Hierdurch kann der Spieler an einer festgelegten Position einen Schluessel aufnehmen
	 * @author Pilz, Konstantin, 5957451
     */
    public void aufnehmenSchluessel(){
    	spieler = fenster.spieler;
    	spieler.nimmSchluessel();

		int posX = spieler.getXPos();
		int posY = spieler.getYPos();
		int itemIDlocal = 1;

		testInstanz.serverAntwort(posX, posY, itemIDlocal);
	}

    /**
     * Methode benutzeHeiltrank:
	 * @author Pilz, Konstantin, 5957451
	 * Methode aus gui-Komponente uebernommen
     */
	public void benutzeHeiltrank() {
		int change = spieler.benutzeHeiltrank();

		// Heilungseffekt wird verbessert, falls neue Monster durch das Aufheben des Schluessels ausgeloest wurden
		if (spieler.hatSchluessel()) {
			spieler.changeHealth((int)(change*1.5));
		}
		else {
			spieler.changeHealth((int) (change * 0.5));
		}
	}


    /**
     * Methode: verwendeSchluessel: Wenn der Schluessel aus diesem Level aufgenommen wurde, kann er verwendet werden
	 * und geht nach Gebrauch verloren. Entsprechend wird Nachricht an Server geschickt.
	 * @author Pilz, Konstantin, 5957451
     */
    public void verwendeSchluessel(){
    	spieler = fenster.spieler;

    	if(spieler.hatSchluessel()) {

    		//Spieler muss nach Schluesselgebrauch ins nächste Level gebracht werden
			//aktuellesLevel.setLevelInhalt(spieler.getXPos(), spieler.getYPos(), 1);
			// neuer Inhalt des des zu veraendernden Ortes hier einfügen

			//Schluessel geht nach Gebrauch verloren
			spieler.entferneSchluessel();
		}
	}

	/**
	 * Methode chatte
	 * @param nachricht
	 * @return
	 */
	public void chatte(String nachricht) {
		boolean cheat = verarbeiteCheat(nachricht);

		if (cheat == false) {
			testInstanz.serverAntwort(nachricht);
		}
	}

	/**
	 * Methode run: Fragt kontinuierlich während der Client geoeffnet ist, ob neue Nachrichten von Server kommen
	 */
	/*public void run() {
		while (client.aktiv) {									//aktiv: ClientServer Verbindung aktiv?
			NachrichtMain m = client.anClientWeitergeben();
			if (m == null) {
				System.out.println("Test");
				try {
					sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				try {
					this.nachrichtenVerarbeitung(m);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		this.interrupt();
	}*/

	/**
	 * Methode nachrichtenVerarbeitung:
	 * @param eingehendeNachricht
	 * @throws Exception
	 */

		/*if (eingehendeNachricht instanceof SpielerBewegung) {
			// Spieler-Bewegung

			System.out.println("Neue Position");
			SpielerBewegung daten = (SpielerBewegung) eingehendeNachricht;
			this.spieler.setPos(daten.neuXPos, daten.neuYPos);
		}
	}*/


	/*public void spielerBewegen(int richtung) {

		//Parameter für Konsistenzcheck benoetigt: Abmessungen des Spielfeldes
		//Hier if check einfügen

		//spieler.setPos(neuPosX, neuPosY);



		int aktuellesLevelWidth = fenster.WIDTH;
		int aktuellesLevelHeight = fenster.HEIGHT;


		spieler = fenster.spieler;
		switch (richtung) {
			case 0:
			*//*
			 * Testet, ob eine Bewegung in die angegebene Richtung moeglich ist.
			 * Fuehrt die Bewegung aus und sendet eine entsprechende Nachricht
			 * an den Server
			 *//*
				if (spieler.getYPos() < aktuellesLevelWidth - 1
						&& fenster.level.getBestimmtenLevelInhalt(spieler.getXPos(), spieler.getYPos() + 1) != 0)

				{
					spieler.runter();
					testInstanz.serverAntwort("spielerXPos: " + spieler.getXPos() + " SpielerYPos: " + spieler.getYPos());//Hier bei bedarf spielerID einfuegen
				}
				break;

			case 1:
			*//*
			 * Analog zu case 0
			 *//*
				if (spieler.getYPos() > 0
						&& fenster.level.getBestimmtenLevelInhalt(spieler.getXPos(), spieler.getYPos() - 1) != 0)
				{
					spieler.hoch();
					testInstanz.serverAntwort("spielerXPos: " + spieler.getXPos() + " SpielerYPos: " + spieler.getYPos());//Hier bei bedarf spielerID einfuegen
				}
				break;

			case 2:
			*//*
			 * Analog zu case 0
			 *//*
				if (spieler.getXPos() > 0
						&& fenster.level.getBestimmtenLevelInhalt(spieler.getXPos() - 1, spieler.getYPos()) != 0)
				{
					spieler.links();
					testInstanz.serverAntwort("spielerXPos: " + spieler.getXPos() + " SpielerYPos: " + spieler.getYPos());//Hier bei bedarf spielerID einfuegen
				}
				break;

			case 3:
			*//*
			 * Analog zu case 0
			 *//*
				if (spieler.getXPos() < aktuellesLevelHeight - 1
						&& fenster.level.getBestimmtenLevelInhalt(spieler.getXPos() + 1, spieler.getYPos()) != 0)
				{
					spieler.rechts();
					testInstanz.serverAntwort("spielerXPos: " + spieler.getXPos() + " SpielerYPos: " + spieler.getYPos());//Hier bei bedarf spielerID einfuegen
				}
				break;

		}
	}*/



	//}


}
