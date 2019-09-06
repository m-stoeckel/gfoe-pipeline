package org.hucompute.textimager.biofid;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.hucompute.textimager.uima.ner.HUComputeNER;
import org.hucompute.textimager.uima.tagme.TagMeAPIAnnotator;
import org.hucompute.textimager.uima.tagme.TagMeLocalAnnotator;
import org.hucompute.textimager.uima.wiki.WikidataHyponyms;
import org.texttechnologylab.annotation.type.Taxon;
import org.texttechnologylab.annotation.type.concept.Person_HumanBeing;

import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.core.languagetool.LanguageToolLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.languagetool.LanguageToolSegmenter;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordNamedEntityRecognizer;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordPosTagger;

public class BioFidPipeline {

	public static void main(String[] args) throws UIMAException, IOException {
		//		JCas cas = JCasFactory.createText("Die Wiesen in Frankfurt am Main sind grün. Frankfurt am Main ist schön.", "de");
		//		DocumentMetaData.create(cas).setDocumentId("test");
		
		//		List<String>lines = FileUtils.readLines(new File("classes.txt"));
		//		ArrayList<String>classes = new ArrayList<>();
		//		for (String string : lines) {
		//			classes.add(string.replace("org.texttechnologylab.annotation.type.", "").replace("=", "\t"));
		//		}
		//		Collections.sort(classes);
		//		classes.stream().forEach(System.out::println);

		//		File[]alreadyProcessed = new File("/resources/public/ahemati/BioFID/BioFID_XMI_taxa_09.05_wahed").listFiles();
		//		String[]fileNamesAlreadyProcessed = new String[alreadyProcessed.length+1];
		//		int count = 1;
		//		fileNamesAlreadyProcessed[0] = "[+]*.xmi"; 
		//		for (File file : alreadyProcessed) {
		//			fileNamesAlreadyProcessed[count] = "[-]"+(file.getName());
		//			System.out.println(fileNamesAlreadyProcessed[count]);
		//			count++;
		//		}
		CollectionReader reader = CollectionReaderFactory.createReader(
				XmiReader.class,
				XmiReader.PARAM_SOURCE_LOCATION,"/resources/public/stoeckel/BioFID/BioFID_XMI_taxa_09.05",
				//								XmiReader.PARAM_SOURCE_LOCATION,"/resources/public/ahemati/BioFID/BioFID_XMI_taxa_09.05_wahed",
				//				XmiReader.PARAM_PATTERNS,fileNamesAlreadyProcessed,
				XmiReader.PARAM_PATTERNS,"[+]366*.xmi",
				XmiReader.PARAM_LENIENT,false,
				XmiReader.PARAM_LANGUAGE,"de"
				);

		AggregateBuilder builder = new AggregateBuilder();
		builder.add(createEngineDescription(BioFidMapper.class,BioFidMapper.PARAM_LANGUAGE,"de"));
		//				builder.add(createEngineDescription(ParagraphSplitter.class,ParagraphSplitter.));
		//				builder.add(createEngineDescription(NERemover.class));
		builder.add(createEngineDescription(LanguageToolSegmenter.class,LanguageToolSegmenter.PARAM_WRITE_TOKEN,false));
		builder.add(createEngineDescription(StanfordPosTagger.class));
		builder.add(createEngineDescription(LanguageToolLemmatizer.class));
		builder.add(createEngineDescription(StanfordNamedEntityRecognizer.class));
		builder.add(createEngineDescription(de.unihd.dbs.uima.annotator.heideltime2.HeidelTime.class));
//				builder.add(createEngineDescription(TagMeLocalAnnotator.class,TagMeLocalAnnotator.PARAM_CONFIG_PATH,"/resources/nlp/models/wikification/tagme/config.sample.xml"));
		builder.add(createEngineDescription(TagMeAPIAnnotator.class,TagMeAPIAnnotator.PARAM_GCUBE_TOKEN,"685b6106-bba0-43e2-87b6-ad8ea0c8f9e2-843339462"));
		builder.add(createEngineDescription(WikidataHyponyms.class));
		builder.add(createEngineDescription(HUComputeNER.class,HUComputeNER.PARAM_CONSTRAINT,true,HUComputeNER.PARAM_CLASS_MAP,"classmap"));
		builder.add(createEngineDescription(
				XmiWriter.class,
				XmiWriter.PARAM_PRETTY_PRINT,true,
				XmiWriter.PARAM_TARGET_LOCATION,"test",
				XmiWriter.PARAM_OVERWRITE,true,
				XmiWriter.PARAM_USE_DOCUMENT_ID,true));

		SimplePipeline.runPipeline(reader, builder.createAggregate());
	}

}
