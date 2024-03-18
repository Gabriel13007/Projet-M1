package main;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gui.Point;
import jeu.Joueur;
import jeu.Plateau;
import jeu.astar.Node;

/**
 * Un joueur dont la stratÃ©gie de jeu est dÃ©finie par
 * {@link #faitUneAction(Plateau) }, Ã utiliser dans le {@link Lanceur} du jeu.
 *
 * @author ???
 */
public class MonJoueur extends Joueur {
	Plateau plateau;
	public MonJoueur(String sonNom) {
		super(sonNom);
	}

	
	@Override
	public Action faitUneAction(Plateau etatDuJeu) {
		plateau = etatDuJeu;
		if (this.donneRessources() < 451) {
			Action action = moveToColline();
			if(action != null) {
				return action;
			}
		} else {
			Action action = moveToFabrique();
			if(action != null) {
				return action;
			}
		}
		return super.faitUneAction(plateau); // a modifier
	}

	@SuppressWarnings("static-access")
	public List<Node> convertToNode(HashMap<Integer, ArrayList<Point>> map, boolean includeColline,
			boolean includeFabrique, boolean includeJoueur) {
		List<Node> lst = new ArrayList<>();

		if (includeColline) {
			ArrayList<Point> lstColline = map.get(1);
			for (Point colline : lstColline) {
				lst.add(new Node(colline.x, colline.y));
			}
		}

		if (includeFabrique) {
			ArrayList<Point> lstFabrique = map.get(2);
			for (Point fabrique : lstFabrique) {
				lst.add(new Node(fabrique.x, fabrique.y));
			}
		}else {
			ArrayList<Point> lstFabrique = map.get(2);
			for (Point fabrique : lstFabrique) {
				if(!estMaFabrique(fabrique)
						|| estRempli(fabrique)) {
					lst.add(new Node(fabrique.x, fabrique.y));
				}
			}
		}

		if (includeJoueur) {
			ArrayList<Point> lstJoueur = map.get(4);
			for (Point joueur : lstJoueur) {
				lst.add(new Node(joueur.x, joueur.y));
			}
		}
		return lst;
	}

	private boolean estRempli(Point fabrique) {
		Map<Point, Integer> mapStock = plateau.donneStocksDesFabriques();
		Integer stock = mapStock.get(fabrique);
		return stock < nbTourRestant();
	}
	
	private boolean estVide(Point fabrique) {
		Map<Point, Integer> mapStock = plateau.donneStocksDesFabriques();
		Integer stock = mapStock.get(fabrique);
		return stock == null || stock == 0;
	}
	
	@SuppressWarnings("static-access")
	private boolean estMaFabrique(Point fabrique) {
		return (donneRang()+1) == plateau.donneUtilisateurDeLaFabrique(plateau.donneContenuCellule(fabrique.x, fabrique.y));
	}

	private Action moveTo(Node node) {
		Point pos = donnePosition();
		if (pos.x < node.x) {
			return Action.DROITE;
		} else if (pos.x > node.x) {
			return Action.GAUCHE;
		} else if (pos.y < node.y) {
			return Action.BAS;
		} else if (pos.y > node.y) {
			return Action.HAUT;
		}
		return Action.RIEN;
	}
	
	@Override 
	public int donneRessourcesPourFabrique(Point p){
		if(plateau != null) {
			Map<Point, Integer> mapStock = plateau.donneStocksDesFabriques();
			Integer stock = mapStock.get(p);
			if(stock != null && nbTourRestant()-stock<donneRessources()) {
				return donneRessources() - stock - nbTourRestant();
			}
		}
		return donneRessources();
	}
	
	private int nbTourRestant() {
		return (plateau.donneNombreDeTours() - plateau.donneTourCourant());
	}
	
	@SuppressWarnings("static-access")
	private Action moveToColline() {
		if (plateau.contientUneColline(plateau.donneContenuCellule(this.donnePosition()))) {
			return Action.RIEN;
		} else {
			int plateauTaille = plateau.donneTaille();
			for (int i = 1; i < plateauTaille; i++) {
				HashMap<Integer, ArrayList<Point>> mapCollineProche = plateau.cherche(this.donnePosition(), i,
						Plateau.CHERCHE_COLLINE);
				if (!mapCollineProche.get(1).isEmpty()) {
					HashMap<Integer, ArrayList<Point>> mapObstacle = plateau.cherche(this.donnePosition(), i,
							Plateau.CHERCHE_TOUT);
					ArrayList<Node> pcc = plateau.donneCheminAvecObstaclesSupplementaires(this.donnePosition(),
							mapCollineProche.get(1).get(0), convertToNode(mapObstacle, false, true, true));
					if(pcc != null) return moveTo(pcc.get(0));
				}
			}
		}
		return null;
	}
	
	@SuppressWarnings("static-access")
	private Action moveToFabrique() {
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
								mapFabriqueProche.get(2).get(0), convertToNode(mapObstacle, true, false, true));
						pcc.toString();
						if(!pcc.isEmpty()) return moveTo(pcc.get(0));
					}
					if(estMaFabrique(fabrique) &&  estRempli(fabrique)) {
						HashMap<Integer, ArrayList<Point>> mapObstacle = plateau.cherche(this.donnePosition(), i,
								Plateau.CHERCHE_TOUT);
						ArrayList<Node> pcc = plateau.donneCheminAvecObstaclesSupplementaires(this.donnePosition(),
								mapFabriqueProche.get(2).get(0), convertToNode(mapObstacle, true, false, true));
						pcc.toString();
						if(!pcc.isEmpty()) return moveTo(pcc.get(0));
					}
				}
			}
		}
		return null;
	}
}