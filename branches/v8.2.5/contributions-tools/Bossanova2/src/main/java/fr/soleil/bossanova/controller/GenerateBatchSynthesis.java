package fr.soleil.bossanova.controller;

import fr.soleil.bossanova.model.Batch;


public class GenerateBatchSynthesis {


	public static String getSynthesis(Batch batch) throws Exception
	{
/*		Velocity.init();

		VelocityContext context = new VelocityContext();

		context.put( "batch", batch );
		context.put( "steps", batch.getSteps() );

		Template template = null;

		try
		{
		   template = Velocity.getTemplate("./synthesis.vm");
		}
		catch( Exception e )
		{
			throw e;
		}

		StringWriter sw = new StringWriter();

		template.merge( context, sw );

		return sw.toString();*/
		return "";
	}
}
