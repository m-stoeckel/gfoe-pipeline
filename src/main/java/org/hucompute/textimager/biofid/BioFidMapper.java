package org.hucompute.textimager.biofid;

import java.io.IOException;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.texttechnologylab.annotation.ocr.OCRParagraph;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaLink;

/**
 * Stanford Part-of-Speech tagger component.
 *
 */
@TypeCapability(
		inputs = {
				"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
		"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" },
		outputs = {"de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS"})
public class BioFidMapper
extends JCasAnnotator_ImplBase
{
	/**
	 * Name of optional configuration parameter that contains the language of the documents in the
	 * input directory. If specified, this information will be added to the CAS.
	 */
	public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
	@ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
	private String language;


	@Override
	public void process(JCas aJCas)
			throws AnalysisEngineProcessException
	{
		for (OCRParagraph ocrParagraph: JCasUtil.select(aJCas, OCRParagraph.class)) {
			Paragraph paragraph = new Paragraph(aJCas, ocrParagraph.getBegin(), ocrParagraph.getEnd());
			paragraph.addToIndexes();
		}

		if(language != null)
		{
			aJCas.setDocumentLanguage(language);	
		}
	}
}
