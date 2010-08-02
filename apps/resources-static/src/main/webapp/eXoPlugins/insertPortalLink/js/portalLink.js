﻿﻿﻿﻿﻿﻿﻿/*
Copyright (c) 2003-2010, CKSource - Frederico Knabben. All rights reserved.
For licensing, see LICENSE.html or http://ckeditor.com/license
*/

var curInstance = '';
CKEDITOR.dialog.add( 'insertPortalLink', function( editor )
{
	curInstance = editor.name;
	return {
		title : 'InsertPortalLink',
		minWidth : 390,
		minHeight : 230,
		contents : [
			{
				id : 'tab1',
				label : '',
				title : '',
				expand : true,
				padding : 0,
				elements :
				[
					{
						type : 'html',
						html :
							'<style type="text/css">' +
									'.ck_about_link .UIFormGrid {'+
										'margin:auto;'+
										'width:auto;'+
									'}'+

									'.ck_about_link .FieldLabel {'+
										'padding:4px;'+
									'}'+
	
									'.ck_about_link .FieldComponent {'+
										'padding:4px;'+
									'}'+

									'.ck_about_link .FieldComponent input {'+
										'width:230px;'+
										'background: white;'+
										'border: 1px solid #c7c7c7;'+
										'color: black;'+
									'}'+

									'.ck_about_link .UIFormGrid .FieldComponent .UIAction {'+
										'padding: 0px;'+
									'}'+

									'.ck_about_link .UIAction {'+
										'padding: 8px 0px;'+
									'}'+

									'.ck_about_link .UIAction .ActionContainer {'+
										'margin:auto;'+
										'width:auto;'+
									'}'+

									'.ck_about_link .UIFormGrid .FieldComponent .UIAction .ActionButton {'+
										'display:block;'+
										'float:none;'+
										'margin:0 3px;'+
										'cursor: pointer;'+
									'}'+
		
									'.ck_about_link .UIAction .ActionButton a:hover {'+
										'color: #045ee8;'+
									'}'+

									'.ck_about_link .UIAction .ActionButton a{'+
										'display: block;'+
										'line-height: 22px;'+
										'height: 22px;'+
									'}'+

									'.ck_about_link .UIAction .LightBlueStyle .ButtonMiddle {'+
										'height: 22px;'+
									'}'+
							'</style>' +
							'<div class="cke_about_container">' +
								'<div class="ck_about_link">'+
								'<table class="UIFormGrid">' +
									'<tbody>' +
										'<tr>' +
											'<td class="FieldLabel">'+
												'<label>Title: </label>'+
											'</td>'+
											'<td colspan="2" class="FieldComponent">'+
												'<input type="text" id="inputTitle">'+
											'</td>'+
										'</tr>'+
										'<tr>'+
											'<td class="FieldLabel">'+
												'<label>URL: </label>'+
											'</td>'+
											'<td class="FieldComponent">'+
												'<input type="text" id="txtUrl">'+
											'</td>'+
											'<td class="FieldComponent">'+
												'<div class="UIAction">'+
													'<table align="center" class="ActionContainer">'+
														'<tbody>'+
															'<tr>'+
																'<td align="center">'+
																	'<div class="ActionButton LightBlueStyle" onclick="getPortalLink();">'+
																		'<div class="ButtonLeft">'+
																			'<div class="ButtonRight">'+
																				'<div class="ButtonMiddle">'+
																					'<label>'+
																						'<a src="http://www.w3.org/1999/xhtml">'+
																							'Get portal link'+
																						'</a>'+
																					'</label>'+
																				'</div>'+
																			'</div>'+
																		'</div>'+
																	'</div>'+
																'</td>'+
															'</tr>'+
														'</tbody>'+
													'</table>'+
												'</div>'+
											'</td>'+
										'</tr>'+
		  						'</tbody>'+
								'</table>'+
								
								'<div class="UIAction">'+
					  			'<table align="center" class="ActionContainer">'+
					    			'<tbody>'+
											'<tr>'+
					      				'<td align="center">'+
					        				'<div class="ActionButton LightBlueStyle" onclick="previewLink();">'+
					          					'<div class="ButtonLeft">'+
					            					'<div class="ButtonRight">'+
					              						'<div class="ButtonMiddle">'+
					              							'<label >'+
																				'<a src="http://www.w3.org/1999/xhtml">'+
																					'Preview'+
																				'</a>'+
																			'</label>'+
					              						'</div>'+
					            					'</div>'+
					          					'</div>'+
					        				'</div>'+
					        				'<div class="ActionButton LightBlueStyle" onclick="addURL();">'+
					          					'<div class="ButtonLeft">'+
					            					'<div class="ButtonRight">'+
					              						'<div class="ButtonMiddle">'+
					                						'<label fcklang="WCMInsertPortalLinkButtonSave">'+
																				'<a src="http://www.w3.org/1999/xhtml">'+
																					'Save'+
																				'</a>'+
																			'</label>'+
					              						'</div>'+
					            					'</div>'+
					          					'</div>'+
					        				'</div>'+
					      				'</td>'+
					    				'</tr>'+
					  				'</tbody>'+
									'</table>'+
	    					'</div>'+
							'</div>'+
						'</div>'
					}
				]
			}
		],
		buttons : [ CKEDITOR.dialog.cancelButton ]
		};
} );

function getPortalLink() {
	var sOptions = "toolbar=no,status=no,resizable=yes,dependent=yes,scrollbars=yes,width=800,height=600" ;
	var newWindow = window.open(CKEDITOR.eXoPath+"eXoPlugins/insertPortalLink/insertPortalLink.html?type=PortalLink", "WCMInsertPortalLink", sOptions );
	newWindow.focus();
}

function previewLink() {
	var sOptions = "toolbar=no, status=no, resizable=yes, dependent=yes, scrollbars=yes,width=800,height=800";
	var url = document.getElementById("txtUrl").value;
	if (url) window.open(url, "", sOptions);
}

function addURL() {
	var url = document.getElementById("txtUrl").value;
	if (url == "") {
		alert("Field URL is not empty"); 
		return;
	}
	var newTag = '<a href="'+url+'">'+url+'</a>';
	CKEDITOR.instances[curInstance].insertHtml(newTag);
	CKEDITOR.dialog.getCurrent().hide();
}