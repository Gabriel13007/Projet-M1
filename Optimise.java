package main;

import java.util.ArrayList;
import java.util.HashMap;

import gui.Point;
import jeu.Plateau;
import jeu.astar.Node;

public class Optimise extends MonJoueur{
	
	public Optimise(String sonNom) {
		super(sonNom);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	@SuppressWarnings("static-access")
	protected Action moveToFabrique() {
		HashMap<Integer, ArrayList<Point>> mapFabriqueProche = plateau.cherche(this.donnePosition(), 500,
				Plateau.CHERCHE_FABRIQUE);
		ArrayList<Node> bestPcc = null;
		for(Point fabrique : mapFabriqueProche.get(2)) {
			if(estVide(fabrique) || estMaFabrique(fabrique) &&  !estRempli(fabrique) ) {
				HashMap<Integer, ArrayList<Point>> mapObstacle = plateau.cherche(this.donnePosition(), 500,
						Plateau.CHERCHE_TOUT);
				ArrayList<Node> pcc = plateau.donneCheminAvecObstaclesSupplementaires(this.donnePosition(),
						fabrique, convertToNode(mapObstacle, false, false, true));
				if(bestPcc == null && pcc != null || pcc != null && pcc.size()<bestPcc.size()) {
					boolean ok = true;
					for(Node node : pcc) {
						int contenuCellule = plateau.donneContenuCellule(node.x, node.y); // joueur @1
						if((contenuCellule & Plateau.MASQUE_PRESENCE_JOUEUR) != 0) {
							ok = false;
						}
					}
					if(ok) bestPcc = pcc;
				}
			}
		}
		
		
		if(bestPcc != null && bestPcc.get(0) != null) return moveTo(bestPcc.get(0));
		return null;
	}

}