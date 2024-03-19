package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import gui.Point;
import jeu.Plateau;
import jeu.astar.Node;

public class ColonOptimise extends MonJoueur{
	
	public ColonOptimise(String sonNom) {
		super(sonNom);
	}
	
	protected boolean estPresqueVide(Point fabrique) {
		Map<Point, Integer> mapStock = plateau.donneStocksDesFabriques();
		Integer stock = mapStock.get(fabrique);
		return stock == null || stock <= 250;
	}
	
	@Override
	protected Action moveToColline() {
		if (estSurColline()) {
			return Action.RIEN;
		} else {
			ArrayList<Point> collineProche = plateau.cherche(this.donnePosition(), 500,
					Plateau.CHERCHE_COLLINE).get(1);
			ArrayList<Node> bestPcc = null;
			for(Point colline : collineProche) {
				int contenuCellule = plateau.donneContenuCellule(colline.x, colline.y);
				if(!Plateau.contientUnJoueur(contenuCellule)) {
					HashMap<Integer, ArrayList<Point>> mapObstacle = plateau.cherche(this.donnePosition(), 500,
							Plateau.CHERCHE_FABRIQUE);
					mapObstacle.putAll(plateau.cherche(this.donnePosition(), 500,
							Plateau.CHERCHE_JOUEUR));
					ArrayList<Node> pcc = plateau.donneCheminAvecObstaclesSupplementaires(this.donnePosition(),
							colline, convertToNode(mapObstacle, false, true, true));
					if(bestPcc == null && pcc != null || pcc != null && pcc.size()<bestPcc.size()) {
						bestPcc = pcc;
					}
				}
			}
			if(bestPcc != null && bestPcc.get(0) != null) return moveTo(bestPcc.get(0));
		}
		return null;
	}
	
	@Override 
	public int donneRessourcesPourFabrique(Point p){
		int don = 0;
		if(plateau != null) {
			Map<Point, Integer> mapStock = plateau.donneStocksDesFabriques();
			Integer stock = mapStock.get(p);
			int donMax;
			if(stock != null) {
				donMax = nbTourRestant() - stock;
			} else {
				donMax = nbTourRestant();
			}
			if(donMax < donneRessources()) {
				don = donMax;
			} else {
				don = donneRessources();
			}
		}
		return don;
//		if(don != 0 && don <= 350) {
//			return don;
//		}else {
//			return 350;
//		}
		//return donneRessources();
	}
	
	@Override
	protected Action moveToFabrique() {
		HashMap<Integer, ArrayList<Point>> mapFabriqueProche = plateau.cherche(this.donnePosition(), 500,
				Plateau.CHERCHE_FABRIQUE);
		ArrayList<Node> bestPccVide = null;
		ArrayList<Node> bestPcc = null;
		for(Point fabrique : mapFabriqueProche.get(2)) {
			if(estVide(fabrique) || (estMaFabrique(fabrique) && estPresqueVide(fabrique))) {
				HashMap<Integer, ArrayList<Point>> mapObstacle = plateau.cherche(this.donnePosition(), 500,
						Plateau.CHERCHE_FABRIQUE);
				mapObstacle.putAll(plateau.cherche(this.donnePosition(), 500,
						Plateau.CHERCHE_JOUEUR));
				ArrayList<Node> pcc = plateau.donneCheminAvecObstaclesSupplementaires(this.donnePosition(),
						fabrique, convertToNode(mapObstacle, false, true, true));
				if(bestPccVide == null && pcc != null || pcc != null && pcc.size()<bestPccVide.size()) {
					boolean ok = true;
					for(Node node : pcc) {
						int contenuCellule = plateau.donneContenuCellule(node.x, node.y); // joueur @1
						if(Plateau.contientUnJoueur(contenuCellule)) {
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
						fabrique, convertToNode(mapObstacle, false, true, true));
				if(bestPcc == null && pcc != null || pcc != null && pcc.size()<bestPcc.size()) {
					boolean ok = true;
					for(Node node : pcc) {
						int contenuCellule = plateau.donneContenuCellule(node.x, node.y); // joueur @1
						if(Plateau.contientUnJoueur(contenuCellule)) {
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
