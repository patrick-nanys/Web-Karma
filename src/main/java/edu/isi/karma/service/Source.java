/*******************************************************************************
 * Copyright 2012 University of Southern California
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/

package edu.isi.karma.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import edu.isi.karma.modeling.alignment.LabeledWeightedEdge;
import edu.isi.karma.modeling.alignment.Name;
import edu.isi.karma.modeling.alignment.NodeType;
import edu.isi.karma.modeling.alignment.Vertex;

public class Source {

	static Logger logger = Logger.getLogger(Source.class);

	public static final String KARMA_SOURCE_PREFIX = "http://isi.edu/integration/karma/sources/";

	private String id;
	private String name;
	private String description;

	private List<Attribute> attributes;
	private Model model;
	private List<String> variables;

	public Source(String id) {
		this.id = id;
		variables = new ArrayList<String>();
		attributes = new ArrayList<Attribute>();
	}
	
	public Source(String id, String name) {
		this.id = id;
		this.setName(name);
		variables = new ArrayList<String>();
		attributes = new ArrayList<Attribute>();
	}
	
	
	public String getUri() {
		return KARMA_SOURCE_PREFIX + getId() + "#";
	}
	public List<Attribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<Attribute> attributes) {
		for (Attribute att : attributes)
			att.setBaseUri(this.getUri());
		this.attributes = attributes;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		if (model != null)
			model.setBaseUri(this.getUri());
		this.model = model;
	}

	public List<String> getVariables() {
		return variables;
	}

	public void setVariables(List<String> variables) {
		this.variables = variables;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public void updateModel(DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> treeModel) {
		
		if (treeModel == null)
			return;
		
		// get the column name associated to the hNodeIds to assign to attribute names 
		// set the rdf ids of all the vertices.  
		for (Vertex v : treeModel.vertexSet()) {
			if (v.getSemanticType() != null && v.getSemanticType().getHNodeId() != null) {
				logger.debug("Vertex " + v.getLocalID() + " is a semantic type associated to a source columns.");
				String attId = "att_" + v.getSemanticType().getHNodeId();
				v.setRdfId(attId);

				Attribute att = new Attribute(attId, this.getUri(), v.getLocalLabel(), IOType.NONE, AttributeRequirement.NONE);
				this.attributes.add(att);
				
			} else {
				logger.debug("Vertex " + v.getLocalID() + " is an intermediate node.");
				String variable = "v" + String.valueOf(variables.size() + 1);
				this.variables.add(variable);
				v.setRdfId(variable);
				continue;
			}
		}

		Model m = new Model("model");
		
		for (Vertex v : treeModel.vertexSet()) {
			
			if (v.getNodeType() == NodeType.DataProperty)
				continue;
			
			if (v.getRdfId() == null)
				continue;
			
			Name classPredicate = new Name(v.getUri(), v.getNs(), v.getPrefix());
			Name argument1 = new Name(v.getRdfId(), null, null);

			ClassAtom classAtom = new ClassAtom(classPredicate, argument1);
			m.getAtoms().add(classAtom);
		}
		
		for (LabeledWeightedEdge e : treeModel.edgeSet()) {
			
			if (e.getSource().getRdfId() == null || e.getTarget().getRdfId() == null)
				continue;

			Name propertyPredicate = new Name(e.getUri(), e.getNs(), e.getPrefix());
			Name argument1 = new Name(e.getSource().getRdfId(), null, null);
			Name argument2 = new Name(e.getTarget().getRdfId(), null, null);

			PropertyAtom propertyAtom = new PropertyAtom(propertyPredicate, argument1, argument2);
			m.getAtoms().add(propertyAtom);
		}
		
		this.setModel(m);
		
	}

	public String getInfo() {
		String s = "";
		s += "uri=" + this.getUri() + "\n";
		s += "id=" + this.getId() + ", ";
		s += "name=" + this.getName() + ", ";
		return s;
	}
	
	public void print() {
		System.out.println("********************************************");
		System.out.println("Source: " + getInfo());
		System.out.println("********************************************");
		System.out.println("Attributes: ");
		for (Attribute p : getAttributes())
			p.print();
		System.out.print("Model: ");
		if (this.model != null) {
			System.out.println(model.getUri());
			this.model.print();
		}
	}

}