package org.georchestra.cadastrapp.service.pdf;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.georchestra.cadastrapp.model.pdf.RelevePropriete;
import org.georchestra.cadastrapp.service.CadController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


public class ReleveProprieteController extends CadController {

	static final Logger logger = LoggerFactory.getLogger(ReleveProprieteController.class);
	
	@Autowired
	ReleveProprieteHelper releveProprieteHelper;

	/**
	 * Create a PDF using a list of comptecommunal
	 * 
	 * @param headers to verify CNIL level information
	 * @param compteCommunal List of ids proprietaires
	 * @return pdf
	 */
	@GET
	@Path("/createRelevePropriete")
	@Produces("application/pdf")
	public Response createRelevePDFPropriete(@Context HttpHeaders headers, @QueryParam("compteCommunal") List<String> comptesCommunaux, @QueryParam("parcelleId") String idParcelle) {

		ResponseBuilder response = Response.noContent();
		
		logger.debug("Controller Parcelle ID (param) : "+idParcelle);
		
		// Check if parcelle list is not empty
		if (comptesCommunaux != null && !comptesCommunaux.isEmpty()) {
			
			// Fixe #329 in some case Extjs return one string with data separated with , instead of a list of data.
			if(comptesCommunaux.size() ==1) {	
				comptesCommunaux = Arrays.asList(comptesCommunaux.get(0).split(","));
			}
			// Get information about releve de propriete
			RelevePropriete relevePropriete = releveProprieteHelper.getReleveProprieteInformation(comptesCommunaux, headers, idParcelle);

			File pdfResult = releveProprieteHelper.generatePDF(relevePropriete, false, false);
			
			// Create response
			response = Response.ok((Object) pdfResult);
			response.header("Content-Disposition", "attachment; filename=" + pdfResult.getName());

		} else {
			logger.warn("Required parameter missing");
		}
		return response.build();
	}

}
