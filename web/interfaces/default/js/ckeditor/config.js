/**
 * @license Copyright (c) 2003-2015, CKSource - Frederico Knabben. All rights reserved.
 * For licensing, see LICENSE.md or http://ckeditor.com/license
 */

CKEDITOR.editorConfig = function( config ) {
	//Preventing convert entities
	config.entities = false;
	// Define changes to default configuration here. For example:
	// config.language = 'fr';
	// config.uiColor = '#AADC6E';

    // Stop CKEditor from removing the attributes from HTML elements in doc.xml
    // e.g. <P ALIGN="JUSTIFY"></P> in the original doc.xml gets turned into
    // <p></p> when viewing the doc in editing mode and then, this ends up
    // getting saved with user changes upon SaveAndRebuild.
    // http://nightly.ckeditor.com/17-10-29-07-04/full/samples/old/datafiltering.html
    // https://sdk.ckeditor.com/samples/acf.html
    // https://stackoverflow.com/questions/15659390/ckeditor-automatically-strips-classes-from-div
    // The following stops CKEDITOR from meddling with existing HTML:
    config.allowedContent = true;
};
