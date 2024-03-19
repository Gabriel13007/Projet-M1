package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gui.Point;
import jeu.Joueur;
import jeu.Plateau;
import jeu.Joueur.Action;
import jeu.astar.Node;

/**
 * Un joueur dont la stratÃ©gie de jeu est dÃ©finie par
 * {@link #faitUneAction(Plateau) }, Ã utiliser dans le {@link Lanceur} du jeu.
 *
 * @author ???
 */
public class JoueurGroupe8 extends Joueur {
	private Plateau plateau;
	public JoueurGroupe8(String sonNom) {
		super(sonNom);
	}

	
	@Override
	public Action faitUneAction(Plateau etatDuJeu) {
		plateau = etatDuJeu;
		if(areAllFull()) {
			return Action.RIEN;
		}
		if ((!estSurColline() && this.donneRessources() < 200) ||
				(estSurColline() && this.donneRessources() < 475)) {
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
	
	private boolean areAllFull() {
		ArrayList<Point> fabriquesProches = plateau.cherche(this.donnePosition(), 500,
				Plateau.CHERCHE_FABRIQUE).get(2);
		for(Point fabrique : fabriquesProches) {
			if(!estRempli(fabrique)) return false;
		}
		return true;
	}

	private List<Node> convertToNode(HashMap<Integer, ArrayList<Point>> map, boolean includeColline,
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
				if(joueur != donnePosition()) {
					lst.add(new Node(joueur.x, joueur.y));
				}
			}
		}
		return lst;
	}

	private boolean estRempli(Point fabrique) {
		Map<Point, Integer> mapStock = plateau.donneStocksDesFabriques();
		Integer stock = mapStock.get(fabrique);
		return stock != null && stock >= nbTourRestant();
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
	}
	
	private int nbTourRestant() {
		return (plateau.donneNombreDeTours() - plateau.donneTourCourant());
	}
	
	private boolean estSurColline() {
		return Plateau.contientUneColline(plateau.donneContenuCellule(this.donnePosition()));
	}
	
	private boolean estPresqueVide(Point fabrique) {
		Map<Point, Integer> mapStock = plateau.donneStocksDesFabriques();
		Integer stock = mapStock.get(fabrique);
		return stock == null || stock <= Math.min(nbTourRestant()-1, 250);
	}
	
	protected boolean estPresqueVideAttaque(Point fabrique) {
		Map<Point, Integer> mapStock = plateau.donneStocksDesFabriques();
		Integer stock = mapStock.get(fabrique);
		return stock == null || stock <= 100;
	}
	
	private ArrayList<Point> getMapJoueurSansMoi() {
		ArrayList<Point> resultat = plateau.cherche(this.donnePosition(), 500,Plateau.CHERCHE_JOUEUR).get(4);
		resultat.remove(donnePosition());
		return resultat;
	}
	
	private Action moveToColline() {
		if (estSurColline()) {
			return Action.RIEN;
		} else {
			ArrayList<Node> bestPccFabrique = getBestPccFabrique();
			ArrayList<Node> pccCollineOpti = null;
			ArrayList<Node> pccCollineDefault = null;
			if(bestPccFabrique != null) {
				Node fabriqueNode = getBestPccFabrique().get(getBestPccFabrique().size()-1);
				Point fabrique = new Point(fabriqueNode.x,fabriqueNode.y);
				ArrayList<Point> collineProche = plateau.cherche(fabrique, 500,
						Plateau.CHERCHE_COLLINE).get(1);
				ArrayList<Node> bestPcc = null;
				Point bestColline = null;
				for(Point colline : collineProche) {
					int contenuCellule = plateau.donneContenuCellule(colline.x, colline.y);
					if(!Plateau.contientUnJoueur(contenuCellule)) {
						HashMap<Integer, ArrayList<Point>> mapObstacle = plateau.cherche(this.donnePosition(), 500,
								Plateau.CHERCHE_FABRIQUE);
						mapObstacle.put(4,getMapJoueurSansMoi());
						ArrayList<Node> pcc = plateau.donneCheminAvecObstaclesSupplementaires(fabrique,
								colline, convertToNode(mapObstacle, false, true, true));
						if(bestPcc == null && pcc != null || pcc != null && pcc.size()<bestPcc.size()) {
							bestPcc = pcc;
							bestColline = colline;
						}
					}
				}
				if(bestColline != null && bestPcc != null && bestPcc.get(0) != null) {
					HashMap<Integer, ArrayList<Point>> mapObstacle = plateau.cherche(this.donnePosition(), 500,
							Plateau.CHERCHE_FABRIQUE);
					mapObstacle.putAll(plateau.cherche(this.donnePosition(), 500,
							Plateau.CHERCHE_JOUEUR));
					ArrayList<Node> pcc = plateau.donneCheminAvecObstaclesSupplementaires(this.donnePosition(),
							bestColline, convertToNode(mapObstacle, false, true, true));
					if(pcc != null) {
						pccCollineOpti = pcc;
						//return moveTo(pcc.get(0));
					}
					
				}
			}
			
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
			if(bestPcc != null && bestPcc.get(0) != null) {
				pccCollineDefault = bestPcc;
				//return moveTo(bestPcc.get(0));
			}
			
			if((pccCollineDefault == null && pccCollineOpti != null) ||
					pccCollineOpti != null && pccCollineOpti.size() == 1) {
				return moveTo(pccCollineOpti.get(0));
			}
			if(pccCollineDefault != null && pccCollineOpti == null||
					pccCollineDefault != null && pccCollineDefault.size() == 1) {
				return moveTo(pccCollineDefault.get(0));
			}
			if(pccCollineDefault == null && pccCollineOpti == null) {
				return null;
			}
			
			if(pccCollineDefault.size()<(pccCollineOpti.size()-5)){
				return moveTo(pccCollineDefault.get(0));
			}else {
				return moveTo(pccCollineOpti.get(0));
			}
		}
	}
	
	private Action moveToFabrique() {
		ArrayList<Node> bestPccFabrique = getBestPccFabrique();
		if(bestPccFabrique != null) { 
			return moveTo(getBestPccFabrique().get(0));
		}
		return null;
	}
	
	private ArrayList<Node> getBestPccFabrique() {
		HashMap<Integer, ArrayList<Point>> mapFabriqueProche = plateau.cherche(this.donnePosition(), 500,
				Plateau.CHERCHE_FABRIQUE);
		ArrayList<Node> bestPccVide = null;
		ArrayList<Node> bestPcc = null;
		ArrayList<Node> bestPccGuetApens = null;
		Integer worstStock = null;
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
			
			if(estPresqueVideAttaque(fabrique)) {
				Map<Point, Integer> mapStock = plateau.donneStocksDesFabriques();
				Integer stock = mapStock.get(fabrique);
				if((worstStock == null && stock != null) || (stock != null && worstStock > stock)) {
					worstStock = stock;
					HashMap<Integer, ArrayList<Point>> mapObstacle = plateau.cherche(this.donnePosition(), 500,
							Plateau.CHERCHE_FABRIQUE);
					mapObstacle.putAll(plateau.cherche(this.donnePosition(), 500,
							Plateau.CHERCHE_JOUEUR));
					bestPccGuetApens = plateau.donneCheminAvecObstaclesSupplementaires(this.donnePosition(),
							fabrique, convertToNode(mapObstacle, false, true, true));
				}
			}
		}
		
		
		if(bestPccVide != null && bestPccVide.get(0) != null) return bestPccVide;
		if(bestPccGuetApens != null && bestPccGuetApens.get(0) != null) return bestPccGuetApens;
		if(bestPcc != null && bestPcc.get(0) != null) return bestPcc;
		return null;
	}
	
	@Override
	protected void finDePartie(String lePlateau) {
		System.out.println();
		if(plateau != null) {
			for(int i=0; i<4; i++) {
				Joueur joueur = plateau.donneJoueur(i);
				System.out.println(joueur.donneNom()+" a "+ joueur.donnePoints()+" points !");
			}
		}
		System.out.println();
	}
}