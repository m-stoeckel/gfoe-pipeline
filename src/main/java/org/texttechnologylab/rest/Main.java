package org.texttechnologylab.rest;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.biofid.gazetter.BIOfidGazetteer;
import org.hucompute.textimager.biofid.BioFidMapper;
import org.hucompute.textimager.uima.ner.HUComputeNER;
import org.hucompute.textimager.uima.tagme.TagMeAPIAnnotator;
import org.hucompute.textimager.uima.wiki.WikidataHyponyms;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.core.languagetool.LanguageToolLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.languagetool.LanguageToolSegmenter;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordNamedEntityRecognizer;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordPosTagger;
import spark.Spark;
import spark.servlet.SparkApplication;
import util.XmlFormatter;

import static spark.Spark.get;


public class Main implements SparkApplication{

	public static void main(final String[] args) throws Exception {
		Main main = new Main();
		main.init();
	}

	public AnalysisEngine getEngine() throws ResourceInitializationException{
		AggregateBuilder builder = new AggregateBuilder();
		builder.add(createEngineDescription(LanguageToolSegmenter.class));
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
		builder.add(createEngineDescription(BIOfidGazetteer.class,
				BIOfidGazetteer.PARAM_LANGUAGE, "de",
				BIOfidGazetteer.PARAM_SOURCE_LOCATION, new String[]{"/resources/public/stoeckel/BioFID/taxaBiofid.txt", "/resources/public/stoeckel/BioFID/wikidataTaxa.txt", "/resources/public/stoeckel/BioFID/taxaOther.txt"},
				BIOfidGazetteer.PARAM_USE_LOWERCASE, true));
		builder.add(createEngineDescription(
				XmiWriter.class,
				XmiWriter.PARAM_PRETTY_PRINT,true,
				XmiWriter.PARAM_TARGET_LOCATION,"test",
				XmiWriter.PARAM_OVERWRITE,true,
				XmiWriter.PARAM_USE_DOCUMENT_ID,true));
		return builder.createAggregate();
	}

	@Override
	public void init() {
		//		Spark.port(7115);
		Spark.exception(Exception.class, (e, request, response) -> {
			final StringWriter sw = new StringWriter();
			final PrintWriter pw = new PrintWriter(sw, true);
			e.printStackTrace(pw);
			System.err.println(sw.getBuffer().toString());
		});

		try {
			AnalysisEngine engine= getEngine();
			get("/process", (request, response) -> {
				String input = request.queryParams("input");
				response.status(200);
				response.type("application/json");
				JCas cas = JCasFactory.createText(input, "de");
				DocumentMetaData meta = DocumentMetaData.create(cas);
				meta.setDocumentId("input");
				engine.process(cas);
				return XmlFormatter.getPrettyString(cas.getCas());
			});	
		} catch (ResourceInitializationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		
	}
}
