# Change Log #


## Version 1.10.0 ##
### New Features ###
  * Import SPSS option to create measure and categories.
  * Import SPSS statistiscs option to import one to many OXML files.
  * Import Correct Encoding Error import option.
  * Problem Import SPSS File view.
  * Character Set Validation import option.
  * Problem Import SPSS Statistics view.
  * Create Instrumention option.
  * Reload Question File button
  * Problem Import Questions view.
  * Option for suppressing graphics for statistics.

### Change in Application Infrastructure ###
  * SPSS import/ export BOM and UTF-8 fix
  * Export study information from legacy database update
  * XQuery insert update
  * DBXml installation not need -DBXml files move to DdiEditor zip file for Windows
  * Added version stamp in exported study units

## Version 1.7.1 ##
### Features ###
  * Added detailed description on error dialogs
  * New element name on editor set id bugfix
  * Deletion of resource bugfix to remove the delete resource in list views

  * IfThenElse Editor -bugfix
  * CategoryScheme Editor -bugfix
  * Variable editor  -name label bugfix
  * CodeScheme Editor fix to lookup rp categories, when selecting rp categories from combobox
  * Dynamic View -bugfix

  * Print/ Stylesheet General UI updates
  * Stylesheet DDI documents may now be printed with or without Variable Universe references (print time parameter). The default value is without references.
  * Stylesheet Line wrapping of long conditions on || and &&
  * Stylesheet Added support for sequence in sequence list
  * Stylesheet Added variable link relations for sub universes
  * Stylesheet Added question/ variable search/ navigation bar from ddixlst support
  * Stylesheet Added suport for concepts

  * DdiEditor-Line plugin -general bugfix
  * ElseCondition bugfix
  * DDIE-189 Fix of wiki question import using defined DDI-L specified creation language
  * Import wiki wizard busy indicator fix

  * JounalStudyInfoExport bugfixes on english title and english subjects
  * Create Genericode Controled Vocabularies from CSV files

  * XmlBeans upgraded to version 2.6.0
  * Editor saving bugfix
  * Editor refactored responcetype
  * Editor removed dublicate EditorInput also defined by EditorPart
  * Osiris-to-ddi-l-batch updates
  * Print function uses XSLT-2.0

## Version 1.6.0 ##
### Features ###
  * Minor updates of menus and seperate tools menu added
  * Import questions bugfix
  * Updates on DDA print stylesheet
  * English DDI-L example of the various stages of dataset to DDI-L workflow
  * Create hirachical classification based on CSV file
  * Export SPSS syntax file based on DDI-L
  * SPSS import change to CVS output data file

## Version 1.5.0 ##
### Features ###
  * Reference to resource package is now supported for the following components: Code Scheme and Category Scheme.
  * Error related to import of SPSS statistics for numeric variable has been corrected.
  * Variable Editor: Response type may now be changed.

## Version 1.4.1 ##
### Features ###
  * New unique study unit and resource package creation with minimal schema requirements
  * Create/ delete menu implemented on resource element types in menu
  * Improved question import dialog
  * DdiEditor-Line support for interviewer instructions
  * Print of statement item and interviewer instruction
  * Statement item editor bug fix
  * Instrument editor bug fix
  * Validate option on Validate View and auto focus
  * Code scheme editor update
  * Dynamic view code/ category table support for non default category scheme
  * Print numeric variables with missing codes
  * Added agency and version support in references on batch meta data creation
  * Variable-, code-, categoryscheme DAO create bug fix
  * Concepts fix on numeric variables ingest
  * Fix of reload of study unit level info

## Version 1.4.0 ##
### Features ###
  * Validate View improved.
  * Stylesheet print-time option added - to control frequence table for numeric variables.
  * Resource Package now supported for Code Schemes.
  * Missing Values now also printed for numeric variables (if frequence table
requested).

## Version 1.3.0 ##
### Features ###
  * Import of CESSDA topics in study topical coverage
  * Support of multi creator
  * Creator affiliation support
  * DDIE-168 - Reimport study universe
  * DDIE-147 - Insert Scheme causing invalid XML
  * DDIE-166 - Import of funding information
  * DDIE-136 - Study unit editor -scroll down of DDI-L XML tab
  * DDIE-136 - Proper creation of missing value attribute on SPSS import
  * DDIE-169 - Print of variable without code scheme but having defined missing codes
  * Variable support of missing codes
  * Text wrap support in frequencies table in DDI-L style sheet

## Version 1.2.0 ##
### Features ###
  * DdiEditor Line support for nested ifthenelse now given in wiki file of
File->Import->Questions.
  * DDA DdiXslt now supports nested ifthenelse.
  * DDIE-163 - Error in createQuestionScheme corrected.
  * DDIE-152 - Print is loaded 2 times
  * DdiEditor SPSS support of long string variables
  * DdiEditor SPSS support of measure export to DDI-L
  * DDIXslt measure support
  * DdiEditor UI DDI-L schema validation feature

## Version 1.1.0 ##
### Features ###
  * Set no compression on DBXml storage, to speed up queries
  * Added Dynamic View feature
  * DDI 3.1 style sheet now supports statistics, missing values and data range.

### Infrastructure ###
  * Refactoring of Respone Type
  * Perspective Utility implementation