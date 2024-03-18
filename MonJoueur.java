package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

	// Utile : plateau.donneStocksDesFabriques()
	@Override
	public Action faitUneAction(Plateau etatDuJeu) {
		plateau = etatDuJeu;
		if (this.donneRessources() < 451) {
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
						return moveTo(pcc.get(0));
					}
				}
			}
		} else {
			int plateauTaille = plateau.donneTaille();
			for (int i = 1; i < plateauTaille; i++) {
				HashMap<Integer, ArrayList<Point>> mapFabriqueProche = plateau.cherche(this.donnePosition(), i,
						Plateau.CHERCHE_FABRIQUE);
				if (!mapFabriqueProche.get(2).isEmpty()) {
					HashMap<Integer, ArrayList<Point>> mapObstacle = plateau.cherche(this.donnePosition(), i,
							Plateau.CHERCHE_TOUT);
					ArrayList<Node> pcc = plateau.donneCheminAvecObstaclesSupplementaires(this.donnePosition(),
							mapFabriqueProche.get(2).get(0), convertToNode(mapObstacle, true, false, true));
					pcc.toString();
					if(!pcc.isEmpty()) return moveTo(pcc.get(0));
				}
			}
		}
		return super.faitUneAction(plateau); // a modifier
	}

	public List<Node> convertToNode(HashMap<Integer, ArrayList<Point>> map, boolean includeColline,
			boolean includeFabrique, boolean includeJoueur) {
		List<Node> lst = new ArrayList<>();

		if (includeColline) {
			ArrayList<Point> lstColline = map.get(1);
			for (int i = 0; i < lstColline.size(); i++) {
				Point colline = lstColline.get(i);
				lst.add(new Node(colline.x, colline.y));
			}
		}

		if (includeFabrique) {
			ArrayList<Point> lstFabrique = map.get(2);
			for (int i = 0; i < lstFabrique.size(); i++) {
				Point fabrique = lstFabrique.get(i);
				lst.add(new Node(fabrique.x, fabrique.y));
			}
		}

		if (includeJoueur) {
			ArrayList<Point> lstJoueur = map.get(4);
			for (int i = 0; i < lstJoueur.size(); i++) {
				Point joueur = lstJoueur.get(i);
				lst.add(new Node(joueur.x, joueur.y));
			}
		}
		return lst;
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
			return donneRessources() - (plateau.donneNombreDeTours() - plateau.donneTourCourant());
		}
		return donneRessources();
	}
}