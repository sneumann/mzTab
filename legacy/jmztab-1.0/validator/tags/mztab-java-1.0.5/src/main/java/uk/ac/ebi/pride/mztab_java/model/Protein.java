package uk.ac.ebi.pride.mztab_java.model;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import uk.ac.ebi.pride.mztab_java.MzTabParsingException;

/**
 * A protein in a mzTab file.
 * @author jg
 *
 */
public class Protein extends TableObject {
	private static final Logger logger = Logger.getLogger(Protein.class);
	
	/**
	 * Available fields
	 */
	private String accession;
	private String unitId;
	private String description;
	private String taxid;
	private String species;
	private String database;
	private String databaseVersion;
	private ParamList searchEngine;
	private ParamList searchEngineScore;
	private Integer reliability;
	private Integer numPeptides;
	private Integer numPeptidesDistinct;
	private Integer numPeptidesUnambiguous;
	private List<String> ambiguityMembers;
	private List<Modification> modifications;
	private URI uri;
	private List<String> goTerms;
	private Double proteinCoverage;
	
	/**
	 * Creates a new empty protein object
	 */
	public Protein() {
		
	}
	
	/**
	 * Creates a new empty protein object based on a parsed
	 * protein table line. This line must be a map with the
	 * column name as key and the respective value as value.
	 * @param parsedTableLine A Map representing a parsed protein table line.
	 */
	public Protein(Map<String, String> parsedTableLine) throws MzTabParsingException {
		try {
			for (String fieldName : parsedTableLine.keySet()) {
				ProteinTableField field = ProteinTableField.getField(fieldName);
				String value 			= parsedTableLine.get(fieldName).trim();
				
				if (field == null) {
					logger.warn("Unknown field <" + fieldName + "> in protein table.");
					continue;
				}
				
				switch (field) {
					case ACCESSION:
						accession = value;
						break;
					case UNIT_ID:
						checkUnitId(value);
						unitId = value;
						break;
					case DESCRIPTION:
						description = value;
						break;
					case TAXID:
						taxid = value;
						break;
					case SPECIES:
						species = value;
						break;
					case DATABASE:
						database = value;
						break;
					case DATABASE_VERSION:
						databaseVersion = value;
						break;
					case SEARCH_ENGINE:
						searchEngine = parseParamListField(value);
						break;
					case SEARCH_ENGINE_SCORE:
						searchEngineScore = parseParamListField(value);
						break;
					case RELIABILITY:
						reliability = parseIntegerField(value);
						// make sure the reliability is between 1-3
						if (reliability != null && (reliability < 1 || reliability > 3))
							throw new MzTabParsingException("Invalid reliability " + reliability + ". Reliability must only be 1 (good), 2 (medium), and 3 (bad).");
						break;
					case NUM_PEPTIDES:
						numPeptides = parseIntegerField(value);
						break;
					case NUM_PEPTIDES_DISTINCT:
						numPeptidesDistinct = parseIntegerField(value);
						break;
					case NUM_PEPTIDES_UNAMBIGUOUS:
						numPeptidesUnambiguous = parseIntegerField(value);
						break;
					case AMBIGUITY_MEMGERS:
						ambiguityMembers = parseStringArray(value, ",");
						break;
					case MODIFICATIONS:
						modifications = parseModifications(value);
						break;
					case URI:
						uri = parseUriField(value);
						break;
					case GO_TERMS:
						goTerms = parseStringArray(value, ",");
						break;
					case PROTEIN_COVERAGE:
						proteinCoverage = parseDoubleField(value);
						break;
					case CUSTOM:
						custom.put(fieldName, value);
						break;
					case PROTEIN_ABUNDANCE:
					case PROTEIN_ABUNDANCE_STD:
					case PROTEIN_ABUNDANCE_STD_ERROR:
						parseAbundanceField(field, fieldName, value);
						break;
				}
			}
		}
		catch (Exception e) {
			throw new MzTabParsingException("Failed to parse protein: " + e.getMessage(), e);
		}
	}

	/**
	 * Parses a protein abundance field (abundance, stdev, stderror) and
	 * sets the appropriate variable.
	 * @param field
	 * @param fieldName
	 * @param value
	 */
	private void parseAbundanceField(ProteinTableField field, String fieldName, String value) {
		// get the subsample index
		Integer subsampleIndex = Integer.parseInt(fieldName.substring(fieldName.lastIndexOf('[') + 1, fieldName.lastIndexOf(']')));
		// parse the value
		Double doubleValue = parseDoubleField(value);
		
		// set the appropriate value
		switch (field) {
			case PROTEIN_ABUNDANCE:
				abundance.put(subsampleIndex, doubleValue);
				break;
			case PROTEIN_ABUNDANCE_STD:
				abundanceStd.put(subsampleIndex, doubleValue);
				break;
			case PROTEIN_ABUNDANCE_STD_ERROR:
				abundanceError.put(subsampleIndex, doubleValue);
				break;
		}
	}

	public String getAccession() {
		return accession;
	}

	public String getUnitId() {
		return unitId;
	}

	public String getDescription() {
		return description;
	}

	public String getTaxid() {
		return taxid;
	}

	public String getSpecies() {
		return species;
	}

	public String getDatabase() {
		return database;
	}

	public String getDatabaseVersion() {
		return databaseVersion;
	}

	public ParamList getSearchEngine() {
		return searchEngine;
	}

	public ParamList getSearchEngineScore() {
		return searchEngineScore;
	}

	public Integer getReliability() {
		return reliability;
	}

	public Integer getNumPeptides() {
		return numPeptides;
	}

	public Integer getNumPeptidesDistinct() {
		return numPeptidesDistinct;
	}

	public Integer getNumPeptidesUnambiguous() {
		return numPeptidesUnambiguous;
	}

	public List<String> getAmbiguityMembers() {
		return ambiguityMembers;
	}

	public List<Modification> getModifications() {
		return modifications;
	}

	public URI getUri() {
		return uri;
	}

	public List<String> getGoTerms() {
		return goTerms;
	}

	public Double getProteinCoverage() {
		return proteinCoverage;
	}
	
	public void setAccession(String accession) throws MzTabParsingException {
		checkStringValue(accession);
		this.accession = accession;
	}

	public void setUnitId(String unitId) throws MzTabParsingException {
		checkUnitId(unitId);
		this.unitId = unitId;
	}

	public void setDescription(String description) throws MzTabParsingException {
		checkStringValue(description);
		this.description = description;
	}

	public void setTaxid(String taxid) throws MzTabParsingException {
		checkStringValue(taxid);
		this.taxid = taxid;
	}

	public void setSpecies(String species) throws MzTabParsingException {
		checkStringValue(species);
		this.species = species;
	}

	public void setDatabase(String database) throws MzTabParsingException {
		checkStringValue(database);
		this.database = database;
	}

	public void setDatabaseVersion(String databaseVersion) throws MzTabParsingException {
		checkStringValue(databaseVersion);
		this.databaseVersion = databaseVersion;
	}

	public void setSearchEngine(ParamList searchEngine) {
		this.searchEngine = searchEngine;
	}

	public void setSearchEngineScore(ParamList searchEngineScore) {
		this.searchEngineScore = searchEngineScore;
	}

	public void setReliability(Integer reliability) throws MzTabParsingException {
		// make sure the reliability is between 1-3
		if (reliability < 1 || reliability > 3)
			throw new MzTabParsingException("Invalid reliability " + reliability + ". Reliability must only be 1 (good), 2 (medium), and 3 (bad).");
		this.reliability = reliability;
	}

	public void setNumPeptides(Integer numPeptides) {
		this.numPeptides = numPeptides;
	}

	public void setNumPeptidesDistinct(Integer numPeptidesDistinct) {
		this.numPeptidesDistinct = numPeptidesDistinct;
	}

	public void setNumPeptidesUnambiguous(Integer numPeptidesUnambiguous) {
		this.numPeptidesUnambiguous = numPeptidesUnambiguous;
	}

	public void setAmbiguityMembers(List<String> ambiguityMembers) throws MzTabParsingException {
		for (String string : ambiguityMembers)
			checkStringValue(string);
		this.ambiguityMembers = ambiguityMembers;
	}

	public void setModifications(List<Modification> modifications) {
		this.modifications = modifications;
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}

	public void setGoTerms(List<String> goTerms) throws MzTabParsingException {
		for (String value : goTerms)
			checkStringValue(value);
		this.goTerms = goTerms;
	}

	public void setProteinCoverage(Double proteinCoverage) {
		this.proteinCoverage = proteinCoverage;
	}

	/**
	 * Converts the protein object to an mzTab formatted
	 * String. The fields are written in the order defined
	 * in the ProteinTableFields ENUM. The header is not
	 * written.
	 * @param nSubsamples Defines the number of subsamples written. In case this protein was not quantified for that many subsamples, MISSING is written instead.
	 * @param optionalColumns A list of optionalColumn headers. In case this protein does not have a value for a given optional column, MISSING is written.
	 * @return The mzTab formatted string representing this protein.
	 */
	@Override
	public String toMzTab(int nSubsamples, List<String> optionalColumns) {
		StringBuffer mzTabString = new StringBuffer();
		List<ProteinTableField> fields = ProteinTableField.getOrderedFieldList();
		
		for (ProteinTableField field : fields) {			
			switch (field) {
				case ROW_PREFIX:
					mzTabString.append((mzTabString.length() > 1 ? SEPARATOR : "") + ProteinTableField.ROW_PREFIX);
					break;
				case ACCESSION:
					mzTabString.append((mzTabString.length() > 1 ? SEPARATOR : "") + toField(accession));
					break;
				case UNIT_ID:
					mzTabString.append((mzTabString.length() > 1 ? SEPARATOR : "") + toField(unitId));
					break;
				case DESCRIPTION:
					mzTabString.append((mzTabString.length() > 1 ? SEPARATOR : "") + toField(description));
					break;
				case TAXID:
					mzTabString.append((mzTabString.length() > 1 ? SEPARATOR : "") + toField(taxid));
					break;
				case SPECIES:
					mzTabString.append((mzTabString.length() > 1 ? SEPARATOR : "") + toField(species));
					break;
				case DATABASE:
					mzTabString.append((mzTabString.length() > 1 ? SEPARATOR : "") + toField(database));
					break;
				case DATABASE_VERSION:
					mzTabString.append((mzTabString.length() > 1 ? SEPARATOR : "") + toField(databaseVersion));
					break;
				case SEARCH_ENGINE:
					mzTabString.append((mzTabString.length() > 1 ? SEPARATOR : "") + toField(searchEngine));
					break;
				case SEARCH_ENGINE_SCORE:
					mzTabString.append((mzTabString.length() > 1 ? SEPARATOR : "") + toField(searchEngineScore));
					break;
				case RELIABILITY:
					mzTabString.append((mzTabString.length() > 1 ? SEPARATOR : "") + toField(reliability));
					break;
				case NUM_PEPTIDES:
					mzTabString.append((mzTabString.length() > 1 ? SEPARATOR : "") + toField(numPeptides));
					break;
				case NUM_PEPTIDES_DISTINCT:
					mzTabString.append((mzTabString.length() > 1 ? SEPARATOR : "") + toField(numPeptidesDistinct));
					break;
				case NUM_PEPTIDES_UNAMBIGUOUS:
					mzTabString.append((mzTabString.length() > 1 ? SEPARATOR : "") + toField(numPeptidesUnambiguous));
					break;
				case AMBIGUITY_MEMGERS:
					mzTabString.append((mzTabString.length() > 1 ? SEPARATOR : "") + arrayToField(ambiguityMembers, ","));
					break;
				case MODIFICATIONS:
					mzTabString.append((mzTabString.length() > 1 ? SEPARATOR : "") + arrayToField(modifications, ","));
					break;
				case URI:
					mzTabString.append((mzTabString.length() > 1 ? SEPARATOR : "") + toField(uri));
					break;
				case GO_TERMS:
					mzTabString.append((mzTabString.length() > 1 ? SEPARATOR : "") + arrayToField(goTerms, ","));
					break;
				case PROTEIN_COVERAGE:
					mzTabString.append((mzTabString.length() > 1 ? SEPARATOR : "") + toField(proteinCoverage));
					break;
			}
		}
		
		// process the abundance data
		mzTabString.append(quantDataToMztab(nSubsamples));
		
		// add the optional columns
		for (String optionalColumn : optionalColumns) {
			mzTabString.append(SEPARATOR + (custom.containsKey(optionalColumn) ? custom.get(optionalColumn) : MISSING));
		}
		
		// add the line terminator
		mzTabString.append(EOL);
		
		return mzTabString.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((accession == null) ? 0 : accession.hashCode());
		result = prime
				* result
				+ ((ambiguityMembers == null) ? 0 : ambiguityMembers.hashCode());
		result = prime * result + ((custom == null) ? 0 : custom.hashCode());
		result = prime * result
				+ ((database == null) ? 0 : database.hashCode());
		result = prime * result
				+ ((databaseVersion == null) ? 0 : databaseVersion.hashCode());
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((goTerms == null) ? 0 : goTerms.hashCode());
		result = prime * result
				+ ((modifications == null) ? 0 : modifications.hashCode());
		result = prime * result
				+ ((numPeptides == null) ? 0 : numPeptides.hashCode());
		result = prime
				* result
				+ ((numPeptidesDistinct == null) ? 0 : numPeptidesDistinct
						.hashCode());
		result = prime
				* result
				+ ((numPeptidesUnambiguous == null) ? 0
						: numPeptidesUnambiguous.hashCode());
		result = prime
				* result
				+ ((abundance == null) ? 0 : abundance.hashCode());
		result = prime
				* result
				+ ((abundanceError == null) ? 0 : abundanceError
						.hashCode());
		result = prime
				* result
				+ ((abundanceStd == null) ? 0 : abundanceStd
						.hashCode());
		result = prime * result
				+ ((proteinCoverage == null) ? 0 : proteinCoverage.hashCode());
		result = prime * result
				+ ((reliability == null) ? 0 : reliability.hashCode());
		result = prime * result
				+ ((searchEngine == null) ? 0 : searchEngine.hashCode());
		result = prime
				* result
				+ ((searchEngineScore == null) ? 0 : searchEngineScore
						.hashCode());
		result = prime * result + ((species == null) ? 0 : species.hashCode());
		result = prime * result + ((taxid == null) ? 0 : taxid.hashCode());
		result = prime * result + ((unitId == null) ? 0 : unitId.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Protein other = (Protein) obj;
		if (accession == null) {
			if (other.accession != null)
				return false;
		} else if (!accession.equals(other.accession))
			return false;
		if (ambiguityMembers == null) {
			if (other.ambiguityMembers != null)
				return false;
		} else if (!ambiguityMembers.equals(other.ambiguityMembers))
			return false;
		if (custom == null) {
			if (other.custom != null)
				return false;
		} else if (!custom.equals(other.custom))
			return false;
		if (database == null) {
			if (other.database != null)
				return false;
		} else if (!database.equals(other.database))
			return false;
		if (databaseVersion == null) {
			if (other.databaseVersion != null)
				return false;
		} else if (!databaseVersion.equals(other.databaseVersion))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (goTerms == null) {
			if (other.goTerms != null)
				return false;
		} else if (!goTerms.equals(other.goTerms))
			return false;
		if (modifications == null) {
			if (other.modifications != null)
				return false;
		} else if (!modifications.equals(other.modifications))
			return false;
		if (numPeptides == null) {
			if (other.numPeptides != null)
				return false;
		} else if (!numPeptides.equals(other.numPeptides))
			return false;
		if (numPeptidesDistinct == null) {
			if (other.numPeptidesDistinct != null)
				return false;
		} else if (!numPeptidesDistinct.equals(other.numPeptidesDistinct))
			return false;
		if (numPeptidesUnambiguous == null) {
			if (other.numPeptidesUnambiguous != null)
				return false;
		} else if (!numPeptidesUnambiguous.equals(other.numPeptidesUnambiguous))
			return false;
		if (abundance == null) {
			if (other.abundance != null)
				return false;
		} else if (!abundance.equals(other.abundance))
			return false;
		if (abundanceError == null) {
			if (other.abundanceError != null)
				return false;
		} else if (!abundanceError.equals(other.abundanceError))
			return false;
		if (abundanceStd == null) {
			if (other.abundanceStd != null)
				return false;
		} else if (!abundanceStd.equals(other.abundanceStd))
			return false;
		if (proteinCoverage == null) {
			if (other.proteinCoverage != null)
				return false;
		} else if (!proteinCoverage.equals(other.proteinCoverage))
			return false;
		if (reliability == null) {
			if (other.reliability != null)
				return false;
		} else if (!reliability.equals(other.reliability))
			return false;
		if (searchEngine == null) {
			if (other.searchEngine != null)
				return false;
		} else if (!searchEngine.equals(other.searchEngine))
			return false;
		if (searchEngineScore == null) {
			if (other.searchEngineScore != null)
				return false;
		} else if (!searchEngineScore.equals(other.searchEngineScore))
			return false;
		if (species == null) {
			if (other.species != null)
				return false;
		} else if (!species.equals(other.species))
			return false;
		if (taxid == null) {
			if (other.taxid != null)
				return false;
		} else if (!taxid.equals(other.taxid))
			return false;
		if (unitId == null) {
			if (other.unitId != null)
				return false;
		} else if (!unitId.equals(other.unitId))
			return false;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}
}
