package main;

import java.util.ArrayList;
import java.util.HashMap;

import gui.Point;
import jeu.Plateau;
import jeu.astar.Node;

public class ColonOptimise extends MonJoueur{
	
	public ColonOptimise(String sonNom) {
		super(sonNom);
	}
	
	@Override
	protected Action moveToFabrique() {
		HashMap<Integer, ArrayList<Point>> mapFabriqueProche = plateau.cherche(this.donnePosition(), 500,
				Plateau.CHERCHE_FABRIQUE);
		ArrayList<Node> bestPccVide = null;
		ArrayList<Node> bestPcc = null;
		for(Point fabrique : mapFabriqueProche.get(2)) {
			if(estVide(fabrique)) {
				HashMap<Integer, ArrayList<Point>> mapObstacle = plateau.cherche(this.donnePosition(), 500,
						Plateau.CHERCHE_FABRIQUE);
				mapObstacle.putAll(plateau.cherche(this.donnePosition(), 500,
						Plateau.CHERCHE_JOUEUR));
				ArrayList<Node> pcc = plateau.donneCheminAvecObstaclesSupplementaires(this.donnePosition(),
						fabrique, convertToNode(mapObstacle, false, false, true));
				if(bestPccVide == null && pcc != null || pcc != null && pcc.size()<bestPccVide.size()) {
					boolean ok = true;
					for(Node node : pcc) {
						int contenuCellule = plateau.donneContenuCellule(node.x, node.y); // joueur @1
						if(contenuCellule == Plateau.ENDROIT_INFRANCHISSABLE) {
							ok = false;
						}
					}
					if(ok) bestPccVide = pcc;
				}
			}
			
			if(estMaFabrique(fabrique) &&  !estRempli(fabrique) ) {
				HashMap<Integer, ArrayList<Point>> mapObstacle = plateau.cherche(this.donnePosition(), 500,
						Plateau.CHERCHE_FABRIQUE);
				mapObstacle.putAll(plateau.cherche(this.donnePosition(), 500,
						Plateau.CHERCHE_JOUEUR));
				ArrayList<Node> pcc = plateau.donneCheminAvecObstaclesSupplementaires(this.donnePosition(),
						fabrique, convertToNode(mapObstacle, false, false, true));
				if(bestPcc == null && pcc != null || pcc != null && pcc.size()<bestPcc.size()) {
					boolean ok = true;
					for(Node node : pcc) {
						int contenuCellule = plateau.donneContenuCellule(node.x, node.y); // joueur @1
						if(contenuCellule == Plateau.ENDROIT_INFRANCHISSABLE) {
							ok = false;
						}
					}
					if(ok) bestPcc = pcc;
				}
			}
		}
		
		
		if(bestPccVide != null && bestPccVide.get(0) != null) return moveTo(bestPccVide.get(0));
		if(bestPcc != null && bestPcc.get(0) != null) return moveTo(bestPcc.get(0));
		return null;
	}

}
