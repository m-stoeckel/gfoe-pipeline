package org.hucompute.textimager.biofid;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.texttechnologylab.annotation.AbstractNamedEntity;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Stanford Part-of-Speech tagger component.
 *
 */
@TypeCapability(
		inputs = {
				"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
		"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" },
		outputs = {"de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS"})
public class BioFidCounter
extends JCasAnnotator_ImplBase
{
	int countSentences = 0;
	int countTokens = 0;
	int countDocuments = 0;
	int countParagraphs = 0;
	HashMap<String, Integer>posCount = new HashMap<>();
	HashMap<String, Integer> neCounts = new HashMap<>();
	HashMap<String, HashSet<String>>instances = new HashMap<>();
	
	@Override
	public void process(JCas aJCas)
			throws AnalysisEngineProcessException
	{
		HashSet<String>startEnd = new HashSet<>();
		countParagraphs += JCasUtil.select(aJCas, Paragraph.class).size();
		countSentences += JCasUtil.select(aJCas, Sentence.class).size();
		countTokens += JCasUtil.select(aJCas, Token.class).size();
		
		Collection<POS> pos = JCasUtil.select(aJCas, POS.class);
		for (POS pos2 : pos) {
			if(!posCount.containsKey(pos2.getPosValue()))
				posCount.put(pos2.getPosValue(), 1);
			else
				posCount.put(pos2.getPosValue(), posCount.get(pos2.getPosValue())+1);
		}
		
		Collection<NamedEntity> ne = JCasUtil.select(aJCas, NamedEntity.class);
		for (NamedEntity nee : ne) {
			if(startEnd.contains(nee.getBegin()+"_"+nee.getEnd()))
				continue;
			else
				startEnd.add(nee.getBegin()+"_"+nee.getEnd());
			if(!neCounts.containsKey(nee.getClass().getName()))
				neCounts.put(nee.getClass().getName(), 1);
			else
				neCounts.put(nee.getClass().getName(), neCounts.get(nee.getClass().getName())+1);
			

			if(!instances.containsKey(nee.getClass().getName()))
				instances.put(nee.getClass().getName(), new HashSet<>());
			else
				instances.get(nee.getClass().getName()).add(nee.getCoveredText());
		}
		
		Collection<AbstractNamedEntity> neA = JCasUtil.select(aJCas, AbstractNamedEntity.class);
		for (AbstractNamedEntity nee : neA) {
			if(startEnd.contains(nee.getBegin()+"_"+nee.getEnd()))
				continue;
			else
				startEnd.add(nee.getBegin()+"_"+nee.getEnd());
			if(!neCounts.containsKey(nee.getClass().getName()))
				neCounts.put(nee.getClass().getName(), 1);
			else
				neCounts.put(nee.getClass().getName(), neCounts.get(nee.getClass().getName())+1);
			

			if(!instances.containsKey(nee.getClass().getName()))
				instances.put(nee.getClass().getName(), new HashSet<>());
			else
				instances.get(nee.getClass().getName()).add(nee.getCoveredText());
		}
		countDocuments++;
		System.out.println(countDocuments);
		
		
	}
	
	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		// TODO Auto-generated method stub
		super.collectionProcessComplete();
		System.out.println(countDocuments);
		System.out.println(countParagraphs);
		System.out.println(countSentences);
		System.out.println(countTokens);
		System.out.println("===pos===");
		posCount.entrySet().stream().forEach(System.out::println);
		System.out.println("===ne===");
		neCounts.entrySet().stream().forEach(System.out::println);
		
		for (Entry<String, HashSet<String>> abstractNamedEntity : instances.entrySet()) {
			try {
				System.out.println(abstractNamedEntity.getKey().replace("org.texttechnologylab.annotation.type.", "") + "\t" + new ArrayList<>(abstractNamedEntity.getValue()).subList(0, abstractNamedEntity.getValue().size()>1000?1000:abstractNamedEntity.getValue().size()).size());
				FileUtils.writeLines(new File("instances/"+abstractNamedEntity.getKey().replace("org.texttechnologylab.annotation.type.", "")), new ArrayList<>(abstractNamedEntity.getValue()).subList(0, abstractNamedEntity.getValue().size()>1000?1000:abstractNamedEntity.getValue().size()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
