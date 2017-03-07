package org.georchestra.cadastrapp.service;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.lang3.StringUtils;
import org.georchestra.cadastrapp.service.export.ExportHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Service to get co owners information
 * 
 * @author pierre jego
 *
 */
public class CoProprietaireController extends CadController {

	static final Logger logger = LoggerFactory.getLogger(CoProprietaireController.class);
	
	@Autowired
	ExportHelper exportHelper;

	@Path("/getCoProprietaireList")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * 
	 * /getCoProprietaireList 
	 * This will return information about owners in JSON format
	 * 
	 * @param headers headers from request used to filter search using LDAP Roles
	 * @param parcelle
	 * 
	 * @return List<Map<String, Object>> Co owners information in key:value List
	 * 
	 * @throws SQLException
	 */
	public List<Map<String, Object>> getCoProprietairesList(@Context HttpHeaders headers, 
			@QueryParam("parcelle") String parcelle, 
			@QueryParam("comptecommunal") String comptecommunal,
			@QueryParam("cgocommune") String cgocommune, 
			@QueryParam("ddenom") String ddenom,
			@DefaultValue("0") @QueryParam("details") int details) throws SQLException {

		List<Map<String, Object>> coProprietaires = new ArrayList<Map<String, Object>>();
		List<String> queryParams = new ArrayList<String>();

		// only for CNIL1 and CNIL2
		if (getUserCNILLevel(headers) > 0 && cgocommune != null && cgocommune.length() >0) {

			boolean isParamValid = false;
			
			StringBuilder queryCoProprietaireBuilder = new StringBuilder();
			
			if(details == 1){
				queryCoProprietaireBuilder.append("select distinct proparc.comptecommunal, prop.app_nom_usage ");   		    	   			    
    		}
    		else{
    			queryCoProprietaireBuilder.append("select prop.app_nom_usage, prop.app_nom_naissance, prop.dlign3, prop.dlign4, prop.dlign5, prop.dlign6, prop.dldnss, prop.jdatnss,prop.ccodro_lib, proparc.comptecommunal ");   			    
    		}
			
			queryCoProprietaireBuilder.append(" from ");
			queryCoProprietaireBuilder.append(databaseSchema);
			queryCoProprietaireBuilder.append(".proprietaire prop, ");
			queryCoProprietaireBuilder.append(databaseSchema);
			queryCoProprietaireBuilder.append(".co_propriete_parcelle proparc ");
			queryCoProprietaireBuilder.append(" where prop.cgocommune = ? ");
			queryParams.add(cgocommune);

			if (parcelle != null && parcelle.length() >0) {
				queryCoProprietaireBuilder.append("and proparc.parcelle = ? ");
				queryParams.add(parcelle);
				isParamValid=true;
			} else if (ddenom != null  && ddenom.length() >0) {
				queryCoProprietaireBuilder.append(" and UPPER(rtrim(prop.app_nom_usage)) LIKE UPPER(rtrim(?)) ");
				queryParams.add("%" + ddenom + "%");
				isParamValid=true;
			} else if (comptecommunal != null  && comptecommunal.length() >0) {
				queryCoProprietaireBuilder.append(" and proparc.comptecommunal = ? ");
				queryParams.add(comptecommunal);
				isParamValid=true;
			} else {
				logger.warn(" Not enough parameters to make the sql call");
			}

			if(isParamValid){
				queryCoProprietaireBuilder.append("and prop.comptecommunal = proparc.comptecommunal ");
				queryCoProprietaireBuilder.append(addAuthorizationFiltering(headers, "prop."));
				JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
				coProprietaires = jdbcTemplate.queryForList(queryCoProprietaireBuilder.toString(), queryParams.toArray());
			}
			
		}
		return coProprietaires;
	}

	@Path("/getCoProprietaire")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * getCoProprietaire
	 * 
	 * @param parcelle
	 * @return
	 */
	public Map<String, Object> getCoProprietaire(@QueryParam("parcelle") String parcelle, @QueryParam("start") int start,@QueryParam("limit") int limit, @Context HttpHeaders headers) {

		logger.debug("get Co Proprietaire - parcelle : " + parcelle);

		Map<String, Object> finalResult = new HashMap<String, Object>();
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

		if (getUserCNILLevel(headers) > 0) {
			
			JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
			
			StringBuilder queryCount = new StringBuilder();
			
			queryCount.append("SELECT COUNT(*) FROM (select distinct p.comptecommunal, p.app_nom_usage from ");
			queryCount.append(databaseSchema);
			queryCount.append(".co_propriete_parcelle propar, ");
			queryCount.append(databaseSchema);
			queryCount.append(".proprietaire p where propar.parcelle = ?  and p.comptecommunal = propar.comptecommunal ");
			queryCount.append(addAuthorizationFiltering(headers, "p."));
			queryCount.append(" ) as temp;");
			
			int resultCount = jdbcTemplate.queryForInt(queryCount.toString(), parcelle);
			
			logger.debug("get Co Proprietaire - number of co-proprietaire : " + resultCount);
			
			finalResult.put("results", resultCount);

			StringBuilder queryBuilder = new StringBuilder();

			// CNIL Niveau 1 or 2
			queryBuilder.append("select distinct p.comptecommunal, p.app_nom_usage, p.dlign3, p.dlign4, p.dlign5, p.dlign6, p.dldnss, p.jdatnss, p.ccodro, p.ccodro_lib");
			queryBuilder.append(" from ");
			queryBuilder.append(databaseSchema);
			queryBuilder.append(".co_propriete_parcelle propar,");
			queryBuilder.append(databaseSchema);
			queryBuilder.append(".proprietaire p where propar.parcelle = ? ");
			queryBuilder.append(" and p.comptecommunal = propar.comptecommunal ");
			queryBuilder.append(addAuthorizationFiltering(headers, "p."));
			queryBuilder.append(" ORDER BY p.app_nom_usage ");
			queryBuilder.append(" LIMIT ").append(limit);
			queryBuilder.append(" OFFSET ").append(start);
			
			result = jdbcTemplate.queryForList(queryBuilder.toString(), parcelle);
			
			finalResult.put("rows", result);

		} else {
			logger.info("User does not have enough right to see information about proprietaire");
		}

		return finalResult;

	}
	
	
	@POST
	@Path("/exportCoProprietaireByParcelles")
	@Produces("text/csv")
	/**
	 * Create a csv file from given parcelles id
	 * 
	 * @param headers used to filter displayed information
	 * @param parcelles list of parcelle separated by a coma
	 * 
	 * @return csv containing list of co-owners of given parcelle list
	 * 
	 * @throws SQLException
	 */
	public Response exportProprietaireByParcelles(
			@Context HttpHeaders headers,
			@FormParam("parcelles") String parcelles) throws SQLException {
		
		// Create empty content
		ResponseBuilder response = Response.noContent();
		
		// User need to be at least CNIL1 level
		if (getUserCNILLevel(headers)>0){
		
			//TODO externalize entete
			String  entete = "Compte communal;Civilité;Nom;Prénom;Nom d'usage;Prénom d'usage;Dénominiation;Nom d'usage;Adresse ligne 3;Adresse ligne 4;Adresse ligne 5;Adresse ligne 6;Identitifiants de parcelles;ccodro_lib";
			if(getUserCNILLevel(headers)>1){
				entete = entete + ";Lieu de naissance; Date de naissance";
			}
			
			String[] parcelleList = StringUtils.split(parcelles, ',');
			
			if(parcelleList != null && parcelleList.length > 0){
				
				logger.debug("Nb of parcelles to search in : " + parcelleList.length);

				// Get value from database
				List<Map<String,Object>> coproprietaires = new ArrayList<Map<String,Object>>();
										
				StringBuilder queryBuilder = new StringBuilder();
				queryBuilder.append("select prop.comptecommunal, ccoqua_lib, dnomus, dprnus, dnomlp, dprnlp, ddenom, app_nom_usage, dlign3, dlign4, dlign5, dlign6, ");
				queryBuilder.append("string_agg(parcelle, ','), ccodro_lib ");
				
				// If user is CNIL2 add birth information
				if(getUserCNILLevel(headers)>1){
					queryBuilder.append(", dldnss, jdatnss ");
				}
				queryBuilder.append("from ");
				queryBuilder.append(databaseSchema);
				queryBuilder.append(".proprietaire prop, ");
				queryBuilder.append(databaseSchema);
				queryBuilder.append(".co_propriete_parcelle proparc ");
				queryBuilder.append(createWhereInQuery(parcelleList.length, "proparc.parcelle"));
				queryBuilder.append(" and prop.comptecommunal = proparc.comptecommunal ");
				queryBuilder.append(addAuthorizationFiltering(headers));
				queryBuilder.append("GROUP BY prop.comptecommunal, ccoqua_lib, dnomus, dprnus, dnomlp, dprnlp, ddenom, app_nom_usage, dlign3, dlign4, dlign5, dlign6, ccodro_lib ");
				// If user is CNIL2 add birth information
				if(getUserCNILLevel(headers)>1){
					queryBuilder.append(", dldnss, jdatnss ");
				}
				queryBuilder.append(" ORDER BY prop.comptecommunal");
				
				JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
				coproprietaires = jdbcTemplate.queryForList(queryBuilder.toString(), parcelleList);
				
				File file = null;

				try {
					// Create file
					file = exportHelper.createCSV(coproprietaires, entete);
									
					// build csv response
					response = Response.ok((Object) file);
					response.header("Content-Disposition", "attachment; filename=" + file.getName());
				}catch (IOException e) {
					logger.error("Error while creating CSV files ", e);
				} finally {
					if (file != null) {
						file.deleteOnExit();
					}
				}
			}
			else{
				//log empty request
				logger.info("Parcelle Id List is empty nothing to search");
			}
		}else{
			logger.info("User does not have rights to see thoses informations");
		}
	
		return response.build();
	}
}
