package main;

import java.util.ArrayList;
import java.util.HashMap;

import gui.Point;
import jeu.Plateau;
import jeu.Joueur.Action;
import jeu.astar.Node;

public class Optimise extends MonJoueur{
	
	public Optimise(String sonNom) {
		super(sonNom);
		// TODO Auto-generated constructor stub
	}
	
//	@Override
//	protected Action moveToColline() {
//		if (estSurColline()) {
//			return Action.RIEN;
//		} else {
//			HashMap<Integer, ArrayList<Point>> mapCollineProche = plateau.cherche(this.donnePosition(), 500,
//					Plateau.CHERCHE_COLLINE);
//			ArrayList<Node> bestPcc = null;
//			
//			for(Point colline : mapCollineProche.get(1)) {
//				ArrayList<Node> pcc = plateau.donneCheminAvecObstaclesSupplementaires(this.donnePosition(),
//						mapCollineProche.get(1).get(0), convertToNode(mapObstacle, false, true, true));
//				for(Node node : pcc) {
//					
//				}
//			}
//			
//			
//			int plateauTaille = plateau.donneTaille();
//			for (int i = 1; i < plateauTaille; i++) {
//				HashMap<Integer, ArrayList<Point>> mapCollineProche = plateau.cherche(this.donnePosition(), i,
//						Plateau.CHERCHE_COLLINE);
//				if (!mapCollineProche.get(1).isEmpty()) {
//					HashMap<Integer, ArrayList<Point>> mapObstacle = plateau.cherche(this.donnePosition(), i,
//							Plateau.CHERCHE_TOUT);
//					ArrayList<Node> pcc = plateau.donneCheminAvecObstaclesSupplementaires(this.donnePosition(),
//							mapCollineProche.get(1).get(0), convertToNode(mapObstacle, false, true, true));
//					if(pcc != null) return moveTo(pcc.get(0));
//				}
//			}
//		}
//		return null;
//	}
	
	@Override
	@SuppressWarnings("static-access")
	protected Action moveToFabrique() {
		HashMap<Integer, ArrayList<Point>> mapFabriqueProche = plateau.cherche(this.donnePosition(), 500,
				Plateau.CHERCHE_FABRIQUE);
		ArrayList<Node> bestPcc = null;
		for(Point fabrique : mapFabriqueProche.get(2)) {
			if(estVide(fabrique) || estMaFabrique(fabrique) &&  !estRempli(fabrique) ) {
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
		
		
		if(bestPcc != null && bestPcc.get(0) != null) return moveTo(bestPcc.get(0));
		return null;
	}

}