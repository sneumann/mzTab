package uk.ac.ebi.pride.jmztab.utils.convert;

import uk.ac.ebi.pride.jaxb.model.*;
import uk.ac.ebi.pride.jaxb.xml.PrideXmlReader;
import uk.ac.ebi.pride.jmztab.model.*;
import uk.ac.ebi.pride.jmztab.model.Param;
import uk.ac.ebi.pride.tools.converter.dao.DAOCvParams;
import uk.ac.ebi.pride.tools.converter.dao.handler.QuantitationCvParams;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static uk.ac.ebi.pride.jmztab.model.MZTabUtils.isEmpty;

/**
 * User: Qingwei
 * Date: 07/06/13
 */
public class ConvertPrideXMLFile extends ConvertFile {
    private PrideXmlReader reader;

    public ConvertPrideXMLFile(File inFile) {
        super(inFile, PRIDE);
        this.reader = new PrideXmlReader(inFile);
        createArchitecture();
        fillData();
    }

    @Override
    protected Metadata convertMetadata() {
        this.metadata = new Metadata();

        // process the references
        loadReferences(reader.getReferences());
        // process the contacts
        loadContacts(reader.getAdmin().getContact());
        // process the experiment params
        loadExperimentParams(reader.getAdditionalParams());
        // process the instrument information
        loadInstrument(reader.getInstrument());
        // set the accession as URI if there's one
        loadURI(reader.getExpAccession());
        // set Ms File
        loadMsRun(reader.getExpAccession());

        return metadata;
    }

    /**
     * Converts the experiment's references into the reference string (DOIs and PubMed ids)
     */
    private void loadReferences(List<Reference> references) {
        if (references == null || references.size() == 0) {
            return;
        }

        for (Reference ref : references) {
            uk.ac.ebi.pride.jaxb.model.Param param = ref.getAdditional();
            if (param == null) {
                continue;
            }

            List<PublicationItem> items = new ArrayList<PublicationItem>();

            // check if there's a DOI
            String doi = getPublicationAccession(param, DAOCvParams.REFERENCE_DOI.getName());
            if (! isEmpty(doi)) {
                items.add(new PublicationItem(PublicationItem.Type.DOI, doi));
            }

            // check if there's a pubmed id
            String pubmed = getPublicationAccession(param, DAOCvParams.REFERENCE_PUBMED.getName());
            if (! isEmpty(pubmed)) {
                items.add(new PublicationItem(PublicationItem.Type.PUBMED, pubmed));
            }

            metadata.addPublicationItems(1, items);
        }
    }

    private String getPublicationAccession(uk.ac.ebi.pride.jaxb.model.Param param, String name) {
        if (param == null || isEmpty(name)) {
            return null;
        }

        // this only makes sense if we have a list of params and an accession!
        for (uk.ac.ebi.pride.jaxb.model.CvParam p : param.getCvParam()) {
            if (name.equals(p.getCvLabel())) {
                return p.getAccession();
            }
        }

        return null;
    }

    /**
     * Converts a list of PRIDE JAXB Contacts into an ArrayList of mzTab Contacts.
     */
    private void loadContacts(List<uk.ac.ebi.pride.jaxb.model.Contact> contactList)  {
        // make sure there are contacts to be processed
        if (contactList == null || contactList.size() == 0) {
            return;
        }

        // initialize the return variable
        int id = 1;
        for (uk.ac.ebi.pride.jaxb.model.Contact c : contactList) {
            metadata.addContactName(id, c.getName());
            metadata.addContactAffiliation(id, c.getInstitution());
            if (c.getContactInfo() != null && c.getContactInfo().contains("@")) {
                metadata.addContactEmail(id, c.getContactInfo());
            }
            id++;
        }
    }

    /**
     * Processes the experiment additional params
     * (f.e. quant method, description...).
     */
    private void loadExperimentParams(uk.ac.ebi.pride.jaxb.model.Param param) {
        if (param == null) {
            return;
        }

        for (uk.ac.ebi.pride.jaxb.model.CvParam p : param.getCvParam()) {
            if (DAOCvParams.EXPERIMENT_DESCRIPTION.getAccession().equals(p.getAccession())) {
                metadata.setDescription(p.getValue());
            } else if (QuantitationCvParams.isQuantificationMethod(p.getAccession())) {
                // check if it's a quantification method
                metadata.setQuantificationMethod(convertParam(p));
            } else if (DAOCvParams.GEL_BASED_EXPERIMENT.getAccession().equals(p.getAccession())) {
                metadata.addCustom(convertParam(p));
            }
        }
    }

    private enum SearchEngineParameter {
        MASCOT("MS", "MS:1001207", "Mascot"),
        OMSSA("MS", "MS:1001475", "OMSSA"),
        SEQUEST("MS", "MS:1001208", "Sequest"),
        SPECTRUMMILL( "MS", "MS:1000687", "Spectrum Mill for MassHunter Workstation"),
        SPECTRAST("MS", "MS:1001477", "SpectaST"),
        XTANDEM_1("MS", "MS:1001476", "X!Tandem"),
        XTANDEM_2("MS", "MS:1001476", "X!Tandem");

        private String cvLabel;
        private String accession;
        private String name;

        private SearchEngineParameter(String cvLabel, String accession, String name) {
            this.cvLabel = cvLabel;
            this.accession = accession;
            this.name = name;
        }
    }

    /**
     * Tries to guess which search engine is described by the passed name. In case no matching parameter
     * is found, null is returned.
     */
    private CVParam findSearchEngineParam(String searchEngineName) {
        if (searchEngineName == null) {
            return null;
        }

        searchEngineName = searchEngineName.toLowerCase();

        SearchEngineParameter param = null;
        if (searchEngineName.contains("mascot")) {
            param = SearchEngineParameter.MASCOT;
        } else if (searchEngineName.contains("omssa")) {
            param = SearchEngineParameter.OMSSA;
        } else if (searchEngineName.contains("sequest")) {
            param = SearchEngineParameter.SEQUEST;
        } else if (searchEngineName.contains("spectrummill")) {
            param = SearchEngineParameter.SPECTRUMMILL;
        } else if (searchEngineName.contains("spectrast")) {
            param = SearchEngineParameter.SPECTRAST;
        } else if (searchEngineName.contains("xtandem")) {
            param = SearchEngineParameter.XTANDEM_1;
        } else if (searchEngineName.contains("x!tandem")) {
            param = SearchEngineParameter.XTANDEM_2;
        }

        if (param == null) {
            return null;
        } else {
            return new CVParam(param.cvLabel, param.accession, param.name, null);
        }
    }

    private CVParam convertParam(uk.ac.ebi.pride.jaxb.model.CvParam param) {
        return new CVParam(param.getCvLabel(), param.getAccession(), param.getName(), param.getValue());
    }

    private uk.ac.ebi.pride.jaxb.model.CvParam getFirstCvParam(uk.ac.ebi.pride.jaxb.model.Param param) {
        if (param == null) {
            return null;
        }

        if (param.getCvParam().iterator().hasNext()) {
            return param.getCvParam().iterator().next();
        }

        return null;
    }

    /**
     * Checks whether the passed identification object is a decoy hit. This function only checks for
     * the presence of specific cv / user Params.
     *
     * @param identification A PRIDE JAXB Identification object.
     * @return Boolean indicating whether the passed identification is a decoy hit.
     */
    private boolean isDecoyHit(Identification identification) {
        for (uk.ac.ebi.pride.jaxb.model.CvParam param : identification.getAdditional().getCvParam()) {
            if (param.getAccession().equals(DAOCvParams.DECOY_HIT.getAccession()))
                return true;
        }

        for (uk.ac.ebi.pride.jaxb.model.UserParam param : identification.getAdditional().getUserParam()) {
            if ("Decoy Hit".equals(param.getName()))
                return true;
        }

        return false;
    }

    private void loadInstrument(uk.ac.ebi.pride.jaxb.model.Instrument instrument) {
        if (instrument == null) {
            return;
        }

        // handle the source information
        uk.ac.ebi.pride.jaxb.model.Param sourceParam = instrument.getSource();
        CvParam param = getFirstCvParam(sourceParam);
        if (param != null) {
            metadata.addInstrumentSource(1, convertParam(param));
        }

        uk.ac.ebi.pride.jaxb.model.Param detectorParam = instrument.getDetector();
        param = getFirstCvParam(detectorParam);
        if (param != null) {
            metadata.addInstrumentDetector(1, convertParam(param));
        }

        // handle the analyzer information
        if (instrument.getAnalyzerList().getCount() > 0) {
            uk.ac.ebi.pride.jaxb.model.Param analyzerParam = instrument.getAnalyzerList().getAnalyzer().iterator().next();
            param = getFirstCvParam(analyzerParam);
            if (param != null) {
                metadata.addInstrumentAnalyzer(1, convertParam(param));
            }
        }

    }

    private void loadURI(String expAccession) {
        if (isEmpty(expAccession)) {
            return;
        }

        try {
            URI uri = new URI("http://www.ebi.ac.uk/pride/experiment.do?experimentAccessionNumber=" + expAccession);
            metadata.addUri(uri);
        } catch (URISyntaxException e) {
            // do nothing
        }
    }

    private void loadMsRun(String expAccession) {
        if (!inFile.isFile()) {
            return;
        }

        metadata.addMsRunFormat(1, new CVParam("MS", "MS:1000564", "PSI mzData file", null));
        metadata.addMsRunIdFormat(1, new CVParam("MS", "MS:1000777", "spectrum identifier nativeID format", null));
        try {
            metadata.addMsRunLocation(1, new URL("ftp://ftp.ebi.ac.uk/pub/databases/pride/PRIDE_Exp_Complete_Ac_" + expAccession + ".xml"));
        } catch (MalformedURLException e) {
            // do nothing
        }
    }

    /**
     * Adds the sample parameters (species, tissue, cell type, disease) to the unit and the various subsamples.
     */
    private void loadSubSamples(SampleDescription sampleDescription) {
        if (sampleDescription == null) {
            return;
        }

        Sample sample1 = null;
        Sample sample2 = null;
        Sample sample3 = null;
        Sample sample4 = null;
        Sample sample5 = null;
        Sample sample6 = null;
        Sample sample7 = null;
        Sample sample8 = null;

        Sample noIdSample = null;
        for (CvParam p : sampleDescription.getCvParam()) {
            // check for subsample descriptions
            if (QuantitationCvParams.SUBSAMPLE1_DESCRIPTION.getAccession().equals(p.getAccession())) {
                if (sample1 == null) {
                    sample1 = new Sample(1);
                    metadata.addSample(sample1);
                }
                sample1.setDescription(p.getValue());
                continue;
            }
            if (QuantitationCvParams.SUBSAMPLE2_DESCRIPTION.getAccession().equals(p.getAccession())) {
                if (sample2 == null) {
                    sample2 = new Sample(2);
                    metadata.addSample(sample1);
                }
                sample2.setDescription(p.getValue());
                continue;
            }
            if (QuantitationCvParams.SUBSAMPLE3_DESCRIPTION.getAccession().equals(p.getAccession())) {
                if (sample3 == null) {
                    sample3 = new Sample(3);
                    metadata.addSample(sample3);
                }
                sample3.setDescription(p.getValue());
                continue;
            }
            if (QuantitationCvParams.SUBSAMPLE4_DESCRIPTION.getAccession().equals(p.getAccession())) {
                if (sample4 == null) {
                    sample4 = new Sample(4);
                    metadata.addSample(sample4);
                }
                sample4.setDescription(p.getValue());
                continue;
            }
            if (QuantitationCvParams.SUBSAMPLE5_DESCRIPTION.getAccession().equals(p.getAccession())) {
                if (sample5 == null) {
                    sample5 = new Sample(5);
                    metadata.addSample(sample5);
                }
                sample5.setDescription(p.getValue());
                continue;
            }
            if (QuantitationCvParams.SUBSAMPLE6_DESCRIPTION.getAccession().equals(p.getAccession())) {
                if (sample6 == null) {
                    sample6 = new Sample(6);
                    metadata.addSample(sample6);
                }
                sample6.setDescription(p.getValue());
                continue;
            }
            if (QuantitationCvParams.SUBSAMPLE7_DESCRIPTION.getAccession().equals(p.getAccession())) {
                if (sample7 == null) {
                    sample7 = new Sample(7);
                    metadata.addSample(sample7);
                }
                sample7.setDescription(p.getValue());
                continue;
            }
            if (QuantitationCvParams.SUBSAMPLE8_DESCRIPTION.getAccession().equals(p.getAccession())) {
                if (sample8 == null) {
                    sample8 = new Sample(8);
                    metadata.addSample(sample8);
                }
                sample8.setDescription(p.getValue());
                continue;
            }

            // check if it belongs to a sample
            if (p.getValue() != null && p.getValue().startsWith("subsample")) {
                // get the subsample number
                Pattern subsampleNumberPattern = Pattern.compile("subsample(\\d+)");
                Matcher matcher = subsampleNumberPattern.matcher(p.getValue());

                if (matcher.find()) {
                    Integer id = Integer.parseInt(matcher.group(1));

                    // remove the value
                    p.setValue(null);

                    Sample sample = metadata.getSampleMap().get(id);
                    if (sample == null) {
                        sample = new Sample(id);
                        metadata.addSample(sample);
                    }

                    // add the param depending on the type
                    if ("NEWT".equals(p.getCvLabel())) {
                        sample.addSpecies(convertParam(p));
                    } else if ("BRENDA".equals(p.getCvLabel())) {
                        sample.addTissue(convertParam(p));
                    } else if ("CL".equals(p.getCvLabel())) {
                        sample.addCellType(convertParam(p));
                    } else if ("DOID".equals(p.getCvLabel()) || "IDO".equals(p.getCvLabel())) {
                        sample.addDisease(convertParam(p));
                    } else if (QuantitationCvParams.isQuantificationReagent(p.getAccession())) {
                        // check if it's a quantification reagent
                        Assay assay = metadata.getAssayMap().get(sample.getId());
                        if (assay == null) {
                            assay = new Assay(sample.getId());
                            assay.setSample(sample);
                            metadata.addAssay(assay);
                        }
                        assay.setQuantificationReagent(convertParam(p));
                    }
                }
            } else {
                noIdSample = new Sample(1);

                // add the param to the "global" group
                if ("NEWT".equals(p.getCvLabel())) {
                    noIdSample.addSpecies(convertParam(p));
                }
                else if ("BTO".equals(p.getCvLabel())) {
                    noIdSample.addTissue(convertParam(p));
                }
                else if ("CL".equals(p.getCvLabel())) {
                    noIdSample.addCellType(convertParam(p));
                }
                else if ("DOID".equals(p.getCvLabel()) || "IDO".equals(p.getCvLabel())) {
                    //DOID: Human Disease Ontology
                    //IDO: Infectious Disease Ontology
                    noIdSample.addDisease(convertParam(p));
                }
            }
        }

        if (noIdSample == null) {
            return;
        }

        SortedMap<Integer, Sample> samples = metadata.getSampleMap();
        if (samples.isEmpty()) {
            metadata.addSample(noIdSample);
        }
    }

    private String getCvParamValue(uk.ac.ebi.pride.jaxb.model.Param param, String accession) {
        if (param == null || isEmpty(accession)) {
            return null;
        }

        // this only makes sense if we have a list of params and an accession!
        for (uk.ac.ebi.pride.jaxb.model.CvParam p : param.getCvParam()) {
            if (accession.equals(p.getAccession())) {
                return p.getValue();
            }
        }

        return null;
    }

    private List<String> getAmbiguityMembers(uk.ac.ebi.pride.jaxb.model.Param param, String accession) {
        if (param == null || isEmpty(accession)) {
            return null;
        }

        // this only makes sense if we have a list of params and an accession!
        List<String> ambiguityMembers = new ArrayList<String>();
        for (uk.ac.ebi.pride.jaxb.model.CvParam p : param.getCvParam()) {
            if (accession.equals(p.getAccession())) {
                ambiguityMembers.add(p.getValue());
            }
        }

        return ambiguityMembers;
    }

    private Collection<Modification> getProteinModifications(List<PeptideItem> items) {
        HashMap<String, Modification> modifications = new HashMap<String, Modification>();

        Modification mod;
        for (PeptideItem item : items) {
            for (ModificationItem ptm : item.getModificationItem()) {
                // ignore modifications that can't be processed correctly
                if (item.getStart() == null || ptm.getModAccession() == null || ptm.getModLocation() == null) {
                    continue;
                }
                mod = modifications.get(ptm.getModAccession());
                if (mod == null) {
                    mod = MZTabUtils.parseModification(Section.Protein, ptm.getModAccession());
                    modifications.put(ptm.getModAccession(), mod);
                }

                Integer position = item.getStart().intValue() + ptm.getModLocation().intValue() - 1;
                mod.addPosition(position, null);
            }
        }

        return modifications.values();
    }

    @Override
    protected MZTabColumnFactory convertProteinColumnFactory() {
        return null;
    }

    @Override
    protected MZTabColumnFactory convertPeptideColumnFactory() {
        return null;
    }

    @Override
    protected MZTabColumnFactory convertPSMColumnFactory() {
        return null;
    }

    @Override
    protected MZTabColumnFactory convertSmallMoleculeColumnFactory() {
        return null;
    }

    @Override
    protected void fillData() {
        // Get a list of Identification ids
        List<String> ids = reader.getIdentIds();

        // Iterate over each identification
        for(String id : ids) {
            Identification identification = reader.getIdentById(id);

            // ignore any decoy hits
            if (isDecoyHit(identification)) {
                continue;
            }

            Protein protein = loadProtein(identification);
            proteins.add(protein);

            // convert the identification's peptides
            List<Peptide> peptideList = loadPeptides(identification);
            peptides.addAll(peptideList);
        }
    }

    /**
     * Converts the passed Identification object into an MzTab protein.
     */
    private Protein loadProtein(Identification identification) {
        // create the protein object
        Protein protein = new Protein(proteinColumnFactory);

//        protein.setAccession(identification.getAccession());
//        protein.setDatabase(identification.getDatabase());
//        protein.setDatabaseVersion(identification.getDatabaseVersion());
//
//        // set the search engine
//        uk.ac.ebi.pride.jmztab.model.Param searchEngineParam = findSearchEngineParam(identification.getSearchEngine());
//        if (searchEngineParam != null) {
//            protein.addSearchEngineParam(searchEngineParam);
//        }
//        // setting the score is not sensible
//
//        // set the description if available
//        String description = getCvParamValue(identification.getAdditional(), DAOCvParams.PROTEIN_NAME.getAccession());
//        protein.setDescription(description);
//
//        // set the species if possible
//        Species species;
//        Unit u = metadata.getUnit(this.unit.getUnitId() + "-sub");
//        if (u != null) {
//            // exists global sub-sample
//            SubUnit subUnit = (SubUnit) u;
//            species = getSpecies(subUnit);
//            if (species != null) {
//                protein.setSpecies(species.getParam().getName());
//                protein.setTaxid(species.getParam().getAccession());
//            }
//        } else {
//            // exists subId sub-samples
//            SortedMap<Integer, SubUnit> subUnits = metadata.getSubUnits();
//            for (SubUnit subUnit : subUnits.values()) {
//                species = getSpecies(subUnit);
//                if (species != null) {
//                    protein.setSpecies(species.getParam().getName());
//                    protein.setTaxid(species.getParam().getAccession());
//                    break;
//                }
//            }
//        }
//
//        // get the number of peptides
//        List<PeptideItem> items = identification.getPeptideItem();
//        protein.setNumPeptides(items.size());
//        HashSet<String> peptideSequences = new HashSet<String>();
//        for (PeptideItem item : items) {
//            List<ModificationItem> modList = item.getModificationItem();
//            StringBuilder sb = new StringBuilder();
//            for (ModificationItem mod : modList) {
//                sb.append(mod.getModAccession()).append(mod.getModLocation());
//            }
//            sb.append(item.getSequence());
//            peptideSequences.add(sb.toString());
//        }
//        // sequence + modifications
//        protein.setNumPeptideDistinct(peptideSequences.size());
//
//        // add the indistinguishable accessions to the ambiguity members
//        List<String> ambiguityMembers = getAmbiguityMembers(identification.getAdditional(), DAOCvParams.INDISTINGUISHABLE_ACCESSION.getAccession());
//        for (String member : ambiguityMembers) {
//            protein.addAmbiguityMembers(member);
//        }
//
//        // set the modifications
//        for (Modification modification : getProteinModifications(items)) {
//            protein.addModification(modification);
//        }
//
//        // add potential quantitative values
//        addAbundanceValues(protein, proteinColumnFactory, identification);
//
//        // process the additional params
//        for (CvParam p : identification.getAdditional().getCvParam()) {
//            // check if there's a quant unit set
//            if (QuantitationCvParams.UNIT_RATIO.getAccession().equals(p.getAccession()) || QuantitationCvParams.UNIT_COPIES_PER_CELL.getAccession().equals(p.getAccession())) {
//                CVParam param = convertParam(p);
//                if (this.unit != null && param != null) {
//                    this.unit.setProteinQuantificationUnit(param);
//                }
//            } else {
//                // check optional column.
//                if (QuantitationCvParams.EMPAI_VALUE.getAccession().equals(p.getAccession())) {
//                    addOptionalColumnValue(protein, proteinColumnFactory, "empai", p.getValue());
//                } else if (DAOCvParams.GEL_SPOT_IDENTIFIER.getAccession().equals(p.getAccession())) {
//                    // check if there's gel spot identifier
//                    addOptionalColumnValue(protein, proteinColumnFactory, "gel_spotidentifier", p.getValue());
//                } else if (DAOCvParams.GEL_IDENTIFIER.getAccession().equals(p.getAccession())) {
//                    // check if there's gel identifier
//                    addOptionalColumnValue(protein, proteinColumnFactory, "gel_identifier", p.getValue());
//                }
//            }
//        }
//
        return protein;
    }

    /**
     * Converts and Identification's peptides into a List of mzTab Peptides.
     */
    private List<Peptide> loadPeptides(Identification identification) {
        List<Peptide> peptideList = new ArrayList<Peptide>();

//        for (PeptideItem peptideItem : identification.getPeptideItem()) {
//            // create the peptide object
//            Peptide peptide = new Peptide(peptideColumnFactory);
//
//            peptide.setSequence(peptideItem.getSequence());
//            peptide.setAccession(identification.getAccession());
//            peptide.setUnitId(unit.getUnitId());
//            peptide.setDatabase(identification.getDatabase());
//            peptide.setDatabaseVersion(identification.getDatabaseVersion());
//
//            // set the peptide spectrum reference
//            String spectrumReference = peptideItem.getSpectrum() == null ? "null" : Integer.toString(peptideItem.getSpectrum().getId());
//            MsFile msFile = unit.getMsFileMap().get(1);
//            peptide.addSpectraRef(new SpecRef(msFile, "spectrum=" + spectrumReference));
//
//            // set the search engine - is possible
//            uk.ac.ebi.pride.jmztab.model.Param searchEngineParam = findSearchEngineParam(identification.getSearchEngine());
//            if (searchEngineParam != null) {
//                peptide.addSearchEngineParam(searchEngineParam);
//            }
//
//            // set the search engine scores
//            loadPeptideSearchEngineScores(peptide, peptideItem);
//
//            // set the modifications
//            for (Modification modification : getPeptideModifications(peptideItem)) {
//                peptide.addModification(modification);
//            }
//
//            // process the quant values
//            addAbundanceValues(peptide, peptideColumnFactory, identification);
//
//            // process the additional params -- mainly check for quantity units
//            if (peptideItem.getAdditional() != null) {
//                for (CvParam p : peptideItem.getAdditional().getCvParam()) {
//                    // check if there's a quant unit set
//                    if (QuantitationCvParams.UNIT_RATIO.getAccession().equals(p.getAccession()) || QuantitationCvParams.UNIT_COPIES_PER_CELL.getAccession().equals(p.getAccession())) {
//                        CVParam param = convertParam(p);
//                        if (param != null) {
//                            unit.setPeptideQuantificationUnit(param);
//                        }
//                    }
//                }
//            }
//
//            peptideList.add(peptide);
//        }

        return peptideList;
    }
}