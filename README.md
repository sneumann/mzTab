## mzTab - _Reporting MS-based Proteomics and Metabolomics Results_


## General
mzTab has been designed to act as a lightweight, tab-delimited file format for mass spec-derived omics data. It was originally designed for proteomics with limited support for metabolomics (version 1.0). The metabolomics aspects are undergoing further development towards full support in a planned version 1.1 update.

One of the main target audiences for this format is researchers outside of proteomics/metabolomics, such as systems biologists. It should be easy to parse and only contain the minimal information required to evaluate the results of an experiment. One of the goals of this file format is that it, for example, should be possible for a biologist to open such a file in Excel and still be able to "see" the data. This format should also become a way to disseminate proteomics and metabolomics results through protocols such as DAS (http://www.biodas.org).

The aim of the format is to present the results of an experiment in a computationally accessible overview. The aim is not to provide the detailed evidence for these results, or allow recreating the process which led to the results. Both of these functions are established through links to more detailed representations in other formats, in particular mzIdentML and mzQuantML for proteomics ID and quantitation.

When you use mzTab format, please cite the following publication:

  * **[J. Griss et al., Mol Cell Proteomics 2014.](http://www.mcponline.org/content/early/2014/06/30/mcp.O113.036681.abstract) [PDF File](http://www.mcponline.org/content/early/2014/06/30/mcp.O113.036681.full.pdf).  [PubMed record](http://www.ncbi.nlm.nih.gov/pubmed/24980485).**


## Specification documents

**Version 1.0.0 (June 2014):**

  > Specification document ([docx](https://github.com/HUPO-PSI/mzTab/tree/master/specification_document-releases/version-1.0.0/mzTab_format_specification.docx), [PDF](https://github.com/HUPO-PSI/mzTab/tree/master/specification_document-releases/version-1.0.0/mzTab_format_specification.pdf))

  > The 20 minute guide to mzTab ([docx](https://github.com/HUPO-PSI/mzTab/tree/master/specification_document-releases/version-1.0.0/20minute_guide_mzTab.docx), [PDF](https://github.com/HUPO-PSI/mzTab/tree/master/specification_document-releases/version-1.0.0/20minute_guide_mzTab.pdf))

## Example Files
Several example of the format can be download from the next link [examples.zip](http://www.ebi.ac.uk/pride/resources/tools/jmztab/latest/examples.zip)

Detailed explanation of all examples can be found [here](../../wiki/Examples)

## jmzTab API

The main principle behind the design of the [**jmzTab**](https://github.com/PRIDE-Utilities/jmztab) core model is to provide an independent light-weight architecture for simplifying the integration of the library in different proteomics/metabolomics software applications. Users can integrate the model into their applications, without the need any other third-party packages. Especially, when users want to recode the model by using other programming languages, and migrates [**jmzTab**](https://github.com/PRIDE-Utilities/jmztab) API into other heterogeneous system.

We provide a [tutorial](https://github.com/PRIDE-Utilities/jmztab/wiki/Home) document and a couple of demos to help you to create [metadata](https://github.com/PRIDE-Utilities/jmztab/wiki/jmzTab-Metadata) and [fill data](https://github.com/PRIDE-Utilities/jmztab/wiki/jmzTab-Columns) by calling [**jmzTab**](https://github.com/PRIDE-Utilities/jmztab) API.

When you use jmzTab library, please cite the following publication:

  * **[Qing-Wei Xu et al., Proteomics 2014; Jun;14(11):1328-32](http://onlinelibrary.wiley.com/doi/10.1002/pmic.201300560/abstract). [PDF File](http://onlinelibrary.wiley.com/doi/10.1002/pmic.201300560/pdf).  [PubMed record](http://www.ncbi.nlm.nih.gov/pubmed/24659499).**

## Wiki
For more information you can visit the [mzTab](https://github.com/HUPO-PSI/mzTab/wiki) and [jmzTab](https://github.com/PRIDE-Utilities/jmztab/wiki) wikis.
