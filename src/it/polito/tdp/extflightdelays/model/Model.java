package it.polito.tdp.extflightdelays.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import it.polito.tdp.extflightdelays.db.ExtFlightDelaysDAO;


public class Model {

	//Ha il grafo	
	SimpleWeightedGraph<Airport, DefaultWeightedEdge> grafo;
	Map<Integer, Airport> aIdMap;
	Map<Airport, Airport> visita;
	
	public Model() {
		grafo = new SimpleWeightedGraph<Airport, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		aIdMap =  new HashMap<Integer,Airport>();
		visita = new HashMap<Airport, Airport>();
	}
	
	public void creaGrafo(int distanzaMedia) {
		
		//I vertici sono tutti gli aeroporti
		ExtFlightDelaysDAO dao = new ExtFlightDelaysDAO();
		dao.loadAllAirports(aIdMap);
		
		//Aggiungo i vertici
		Graphs.addAllVertices(grafo, aIdMap.values());
		
		//Aggiungo gli archi
		for(Rotta rotta: dao.getRotte(aIdMap, distanzaMedia)) {
			
			//Controllo se esiste gia un arco tra i due
			//Se esiste aggiorno il peso
			DefaultWeightedEdge edge = grafo.getEdge(rotta.getPartenza(), rotta.getDestinazione());
			
			if(edge == null) {
				Graphs.addEdge(grafo, rotta.getPartenza(), rotta.getDestinazione(), rotta.getDistanzaMedia());
			} else {
				
				double peso = grafo.getEdgeWeight(edge);
				double newPeso = (peso + rotta.getDistanzaMedia())/2;
				
//				System.out.println("Aggiornare peso! Peso vecchio: "+peso+", peso nuovo: "+newPeso);
				
				grafo.setEdgeWeight(edge, newPeso);
			}		
			
		}
		
		System.out.println("Grafo creato!");
		System.out.println("Vertici: "+grafo.vertexSet().size());
		System.out.println("Archi: "+grafo.edgeSet().size());
		
		
	}
	
	
	public Boolean testConnessione(Integer a1, Integer a2) {
		
		Set<Airport> visitati = new HashSet<Airport>();
		Airport partenza = aIdMap.get(a1);
		Airport destinazione = aIdMap.get(a2);
		
		System.out.println("Testo connessione tra "+partenza+" e " +destinazione);
		
		//Visito in ampiezza (utile per il punto dopo per cercare il percorso)
		BreadthFirstIterator<Airport, DefaultWeightedEdge> it = new BreadthFirstIterator<Airport, DefaultWeightedEdge>(grafo, partenza);
		
		while(it.hasNext()) {
			visitati.add(it.next());
		}
		
		if(visitati.contains(destinazione))
			return true;
		else 
			return false;
		
	}
	
	public List<Airport> trovaPercorso(Integer a1, Integer a2) {
		
		Airport partenza = aIdMap.get(a1);
		Airport destinazione = aIdMap.get(a2);
		List<Airport> percorso = new ArrayList<Airport>();
		
		System.out.println("Cerco percorso tra "+partenza+" e " +destinazione);
		
		BreadthFirstIterator<Airport, DefaultWeightedEdge> it = new BreadthFirstIterator<Airport, DefaultWeightedEdge>(grafo, partenza);
		visita.put(partenza, null);	
		it.addTraversalListener(new TraversalListener<Airport, DefaultWeightedEdge>() {
			
			@Override
			public void vertexTraversed(VertexTraversalEvent<Airport> arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void vertexFinished(VertexTraversalEvent<Airport> arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void edgeTraversed(EdgeTraversalEvent<DefaultWeightedEdge> ev) {
				
				//trovo gli estremi dell'arco
				Airport sorgente = grafo.getEdgeSource(ev.getEdge()); //parent
				Airport destinazione = grafo.getEdgeTarget(ev.getEdge()); //child
				
				//Soddisfano le condizioni sopra?
				
				/*
				 * Se il grafo � orientato source==parent, target==child
				 * Se il grafo non � orientato potrebbe anche essere il contrario
				 * 
				 */
				
				//Child � nuovo? Controllo se � nella mappa
				
				if( !visita.containsKey(destinazione) && visita.containsKey(sorgente)) {			
					visita.put(destinazione, sorgente);
					
				} else if(visita.containsKey(destinazione) && !visita.containsKey(sorgente)) {			
					visita.put(sorgente, destinazione);
				}
				
			}
			
			@Override
			public void connectedComponentStarted(ConnectedComponentTraversalEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void connectedComponentFinished(ConnectedComponentTraversalEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		while(it.hasNext()) {
			it.next();
		}
		
		if(!visita.containsKey(partenza) || !visita.containsKey(destinazione))
			return null;
		
		Airport step = destinazione;
		while(!step.equals(partenza)) {
			percorso.add(step);
			step = visita.get(step);
		}
		percorso.add(step);
		return percorso;
	}
}
