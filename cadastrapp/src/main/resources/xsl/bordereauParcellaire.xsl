<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
	xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:java="http://xml.apache.org/xslt/java" exclude-result-prefixes="java">

	<!-- Page layout information -->
	<xsl:template match="/">
		<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">

			<fo:layout-master-set>
				<fo:simple-page-master master-name="portrait"
					page-height="21cm" page-width="29.7cm" font-family="sans-serif"
					margin-top="0.5cm" margin-bottom="0.5cm" margin-left="0.5cm"
					margin-right="0.5cm">
					<fo:region-body />
				</fo:simple-page-master>
			</fo:layout-master-set>

			<fo:page-sequence master-reference="portrait">
				<fo:flow flow-name="xsl-region-body">
					<xsl:apply-templates select="bordereauParcellaire" />
				</fo:flow>
			</fo:page-sequence>

		</fo:root>
	</xsl:template>

	<!-- Definition des styles -->
	
	<!-- Format titre -->
	<xsl:attribute-set name="titre">
		<xsl:attribute name="text-align">center</xsl:attribute>
		<xsl:attribute name="padding-top">10pt</xsl:attribute>
		<xsl:attribute name="padding-bottom">10pt</xsl:attribute>
		<xsl:attribute name="font-size">14pt</xsl:attribute>
		<xsl:attribute name="font-weight">bold</xsl:attribute>
	</xsl:attribute-set>
	
	<!-- Format de text simple -->
	<xsl:attribute-set name="text">
		<xsl:attribute name="text-align">left</xsl:attribute>
		<xsl:attribute name="font-size">9pt</xsl:attribute>
	</xsl:attribute-set>

	<!-- Format de text gras -->
	<xsl:attribute-set name="text-bold">
		<xsl:attribute name="text-align">center</xsl:attribute>
		<xsl:attribute name="padding-top">10pt</xsl:attribute>
		<xsl:attribute name="padding-bottom">10pt</xsl:attribute>
		<xsl:attribute name="font-size">10pt</xsl:attribute>
		<xsl:attribute name="font-weight">bold</xsl:attribute>
	</xsl:attribute-set>

	<xsl:template match="bordereauParcellaire">

		<xsl:variable name="dateDeCreation">
			<xsl:value-of
				select="java:format(java:java.text.SimpleDateFormat.new('d MMMM yyyy'), java:java.util.Date.new())" />
		</xsl:variable>
		<xsl:variable name="service">
			<xsl:value-of select="service" />
		</xsl:variable>
		<xsl:variable name="serviceUrl">
			<xsl:value-of select="serviceUrl" />
		</xsl:variable>
		<xsl:variable name="dateDeValiditeMajic">
			<xsl:value-of select="dateDeValiditeMajic" />
		</xsl:variable>
		<xsl:variable name="dateDeValiditeEdigeo">
			<xsl:value-of select="dateDeValiditeEdigeo" />
		</xsl:variable>
		<xsl:variable name="fillColor">
			<xsl:value-of select="style/@fillColor" />
		</xsl:variable>
		<xsl:variable name="fillOpacity">
			<xsl:value-of select="style/@fillOpacity" />
		</xsl:variable>
		<xsl:variable name="strokeColor">
			<xsl:value-of select="style/@strokeColor" />
		</xsl:variable>
		<xsl:variable name="strokeWidth">
			<xsl:value-of select="style/@strokeWidth" />
		</xsl:variable>
		<xsl:variable name="baseMapIndex">
			<xsl:value-of select="baseMapIndex" />
		</xsl:variable>
		
		<xsl:for-each select="parcelles/parcelle">
			<fo:table table-layout="fixed" page-break-before="always">
				<fo:table-column column-width="72%" />
				<fo:table-column column-width="28%" />
				<fo:table-body>
					<fo:table-row>
						<!-- Empreinte -->
						<fo:table-cell>
							<fo:block>
								<fo:external-graphic  content-width="scale-to-fit" content-height="100%" width="100%" scaling="uniform">
									<xsl:attribute name="src">
										<xsl:choose>
											<!--  with given style -->
											<xsl:when test="$fillColor">								
												<xsl:value-of select="$serviceUrl" />/getImageBordereau?parcelle=<xsl:value-of select="@parcelleId" /><![CDATA[&]]>fillcolor=<xsl:value-of select="$fillColor" /><![CDATA[&]]>fillopacity=<xsl:value-of select="$fillOpacity" /><![CDATA[&]]>strokecolor=<xsl:value-of select="$strokeColor" /><![CDATA[&]]>strokewidth=<xsl:value-of select="$strokeWidth" /><![CDATA[&]]>basemapindex=<xsl:value-of select="$baseMapIndex" /> 
											</xsl:when>
											<!--  without style -->
											<xsl:otherwise>			
												<xsl:value-of select="$serviceUrl" />/getImageBordereau?parcelle=<xsl:value-of select="@parcelleId" />
											</xsl:otherwise>	
										</xsl:choose>
									</xsl:attribute>
								</fo:external-graphic>
							</fo:block>
						</fo:table-cell>
						<!-- descriptif -->
						<fo:table-cell>

							<fo:block xsl:use-attribute-sets="titre">
								Extrait du plan	cadastral informatisé
							</fo:block>

							<fo:block xsl:use-attribute-sets="text" padding-top="10pt">
								Données foncières valides au
								<xsl:value-of select="$dateDeValiditeMajic" />
							</fo:block>
							<fo:block xsl:use-attribute-sets="text">
								Données cartographiques valides au
								<xsl:value-of select="$dateDeValiditeEdigeo" />
							</fo:block>
							<fo:block xsl:use-attribute-sets="text">
								Document créé le
								<xsl:value-of select="$dateDeCreation" />
							</fo:block>
							<fo:block xsl:use-attribute-sets="text" padding-top="10pt">
								Fond de plan origine DGFiP - Reproduction interdite
							</fo:block>
							
							<fo:block padding-top="8pt" font-size="7pt">
								Les informations contenues dans ce document sont réservées à l'usage personnel du demandeur (art L107 B du livre des procédures fiscales). Vous ne pouvez pas communiquer d'informations à caractère personnel à des tiers sans accord express des personnes concernées par ces données (chap II art 13 loi Informatique et Libertés de 1978 modifiée 2004).
Les informations contenues dans ce document sont les plus à jour dans la mesure des capacités des responsables du logiciel à les maintenir à jour.
							</fo:block>
							<fo:block xsl:use-attribute-sets="text-bold" padding-top="10pt">
								<xsl:value-of select="@libelleCommune" />
							</fo:block>
							<fo:block xsl:use-attribute-sets="text" >
								Parcelle cadastrale
								<xsl:value-of select="@parcelleId" />
							</fo:block>

							<fo:block xsl:use-attribute-sets="text" padding-top="10pt">
								<xsl:value-of select="@adresseCadastrale" />
							</fo:block>

							<fo:table table-layout="fixed">
								<fo:table-column column-width="15%" />
								<fo:table-column column-width="15%" />
								<fo:table-column column-width="35%" />
								<fo:table-column column-width="35%" />
								<fo:table-body padding-top="10pt">
									<fo:table-row>
										<fo:table-cell>
											<fo:block xsl:use-attribute-sets="text">
												section
											</fo:block>
										</fo:table-cell>
										<fo:table-cell>
											<fo:block xsl:use-attribute-sets="text">
												parcelle
											</fo:block>
										</fo:table-cell>
										<fo:table-cell>
											<fo:block xsl:use-attribute-sets="text">
												code de la voie
											</fo:block>
										</fo:table-cell>
										<fo:table-cell>
											<fo:block xsl:use-attribute-sets="text">
												contenance DGFiP
											</fo:block>
										</fo:table-cell>
									</fo:table-row>
									<fo:table-row>
										<fo:table-cell>
											<fo:block xsl:use-attribute-sets="text">
												<xsl:value-of select="@section" />
											</fo:block>
										</fo:table-cell>
										<fo:table-cell>
											<fo:block xsl:use-attribute-sets="text">
												<xsl:value-of select="@parcelle" />
											</fo:block>
										</fo:table-cell>
										<fo:table-cell>
											<fo:block xsl:use-attribute-sets="text">
												<xsl:value-of select="@codeFantoir" />
											</fo:block>
										</fo:table-cell>
										<fo:table-cell>
											<fo:block xsl:use-attribute-sets="text">
												<xsl:value-of select="@surfaceCadastrale" />
											</fo:block>
										</fo:table-cell>
									</fo:table-row>
								</fo:table-body>
							</fo:table>

							<!--  owner date only if cnil1 right -->
							<fo:block-container padding-top="10pt" height="8cm">
								<fo:block/>
								<xsl:if test="proprietaires">
									<xsl:for-each select="proprietaires/proprietaire">
										<fo:block text-align="left" padding-top="5pt"
											padding-left="5pt" font-size="10pt">
											<xsl:value-of select="@nom" />
										</fo:block>
										<fo:block text-align="left" padding-bottom="5pt"
											padding-left="5pt" font-size="8pt">
											<xsl:value-of select="@adresse" />
										</fo:block>
									</xsl:for-each>
								</xsl:if>
							</fo:block-container>

							<fo:block xsl:use-attribute-sets="text-bold">
								<xsl:value-of select="$service" />
							</fo:block>

						</fo:table-cell>
					</fo:table-row>
				</fo:table-body>
			</fo:table>
		</xsl:for-each>
	</xsl:template>

</xsl:stylesheet>
