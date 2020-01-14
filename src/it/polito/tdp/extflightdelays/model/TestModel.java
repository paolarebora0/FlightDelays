package it.polito.tdp.extflightdelays.model;

import org.jgrapht.Graph;

public class TestModel {

	public static void main(String[] args) {
		
		Model model = new Model();

		int distanzaMedia = 400;
		model.creaGrafo(distanzaMedia);
		
		if(model.testConnessione(11, 17))
			System.out.println("Connessi");
		else 
			System.out.println("Non connessi");
		
		System.out.println(model.trovaPercorso(11, 297));
	}

}
