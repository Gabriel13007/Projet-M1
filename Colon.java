package main;

import java.util.ArrayList;
import java.util.HashMap;

import gui.Point;
import jeu.Plateau;
import jeu.Joueur.Action;
import jeu.astar.Node;

public class Colon extends MonJoueur{
	//Test
	public Colon(String sonNom) {
		super(sonNom);
	}
	@Override
	protected Action moveToFabrique() {
		int plateauTaille = plateau.donneTaille();
		for (int i = 1; i < plateauTaille; i++) {
			HashMap<Integer, ArrayList<Point>> mapFabriqueProche = plateau.cherche(this.donnePosition(), i,
					Plateau.CHERCHE_FABRIQUE);
			if (!mapFabriqueProche.get(2).isEmpty()) {
				for(Point fabrique : mapFabriqueProche.get(2)) {
					if(estVide(fabrique)) {
						HashMap<Integer, ArrayList<Point>> mapObstacle = plateau.cherche(this.donnePosition(), i,
								Plateau.CHERCHE_TOUT);
						ArrayList<Node> pcc = plateau.donneCheminAvecObstaclesSupplementaires(this.donnePosition(),
								fabrique, convertToNode(mapObstacle, false, false, true));
						if(pcc != null && pcc.get(0) != null) return moveTo(pcc.get(0));
					}
				}
				for(Point fabrique : mapFabriqueProche.get(2)) {
					if(estMaFabrique(fabrique) &&  !estRempli(fabrique)) {
						HashMap<Integer, ArrayList<Point>> mapObstacle = plateau.cherche(this.donnePosition(), i,
								Plateau.CHERCHE_TOUT);
						ArrayList<Node> pcc = plateau.donneCheminAvecObstaclesSupplementaires(this.donnePosition(),
								fabrique, convertToNode(mapObstacle, false, false, true));
						if(pcc != null) return moveTo(pcc.get(0));
					}
				}
			}
		}
		return null;
	}

}
