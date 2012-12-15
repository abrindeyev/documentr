<%--
documentr - Edit, maintain, and present software documentation on the web.
Copyright (C) 2012 Maik Schreiber

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="d" uri="http://documentr.org/tld/documentr" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags" %>

<sec:authorize access="(#pageForm.path == null) ?
	hasBranchPermission(#pageForm.projectName, #pageForm.branchName, EDIT_PAGE) :
	hasPagePermission(#pageForm.projectName, #pageForm.branchName, #pageForm.path, EDIT_PAGE)">

<c:choose>
	<c:when test="${!empty pageForm.path}"><c:set var="hierarchyPagePath" value="${pageForm.path}"/></c:when>
	<c:when test="${!empty pageForm.parentPagePath}"><c:set var="hierarchyPagePath" value="${pageForm.parentPagePath}"/></c:when>
	<c:otherwise><c:set var="hierarchyPagePath" value="home"/></c:otherwise>
</c:choose>

<dt:headerJS>

var allTags = [];
var allTagsLoaded = false;
var dirty = false;
var jqXHRs = [];

function togglePreview() {
	var previewEl = $('#preview');
	if (previewEl.length === 0) {
		var el = $('.editor-wrapper');
		var text = $('#editor').data('editor').getValue();
		$.ajax({
			url: '<c:url value="/page/markdownToHtml/${pageForm.projectName}/${pageForm.branchName}/json"/>',
			type: 'POST',
			dataType: 'json',
			data: {
				pagePath: '<c:out value="${hierarchyPagePath}"/>',
				markdown: text
			},
			success: function(result) {
				$('#textEditorToolbar a').each(function() {
					$(this).setButtonDisabled(true);
				});
				$('#togglePreviewButton').setButtonDisabled(false);

				previewEl = $('<div id="preview" class="preview"></div>');
				previewEl.html(result.html);
				$(document.body).append(previewEl);
				documentr.setupCodeViews();
				previewEl
					.css('left', el.offset().left)
					.css('top', el.offset().top)
					.css('width', el.outerWidth() - (previewEl.outerWidth() - previewEl.width()))
					.css('height', el.outerHeight() - (previewEl.outerHeight() - previewEl.height()))
					.slideToggle('fast');
			}
		});
	} else {
		previewEl.slideToggle('fast', function() {
			previewEl.remove();
		});

		$('#textEditorToolbar a').each(function() {
			$(this).setButtonDisabled(false);
		});
	}
}

function toggleStyleBold() {
	var editor = $('#editor').data('editor');
	var origRange = editor.getSelectionRange();
	var lenBefore = editor.session.getTextRange(origRange).length;
	editor.selection.shiftSelection(!editor.selection.isBackwards() ? -2 : 2);
	if (!editor.selection.isBackwards()) {
		for (var i = 1; i <= 4; i++) {
			editor.selection.selectRight();
		}
	} else {
		for (var i = 1; i <= 4; i++) {
			editor.selection.selectLeft();
		}
	}
	var text = editor.session.getTextRange(editor.getSelectionRange());
	var lenAfter = text.length;
	if ((lenAfter - lenBefore) === 4) {
		var isBold = (text.substring(0, 2) === '**') && (text.substring(text.length - 2, text.length) === '**');
		var origText = editor.session.getTextRange(origRange);
		if (!isBold) {
			editor.session.replace(origRange, '**' + origText + '**');
		} else {
			editor.session.replace(editor.getSelectionRange(), origText);
		}
		editor.selection.setSelectionRange(origRange);
		editor.selection.shiftSelection(!isBold ? 2 : -2);
		editor.focus();
	}
}

function toggleStyleItalic() {
	var editor = $('#editor').data('editor');
	var origRange = editor.getSelectionRange();
	var lenBefore = editor.session.getTextRange(origRange).length;
	editor.selection.shiftSelection(!editor.selection.isBackwards() ? -1 : 1);
	if (!editor.selection.isBackwards()) {
		for (var i = 1; i <= 2; i++) {
			editor.selection.selectRight();
		}
	} else {
		for (var i = 1; i <= 2; i++) {
			editor.selection.selectLeft();
		}
	}
	var text = editor.session.getTextRange(editor.getSelectionRange());
	var lenAfter = text.length;
	if ((lenAfter - lenBefore) === 2) {
		var isItalic = (text.substring(0, 1) === '*') && (text.substring(text.length - 1, text.length) === '*');
		var origText = editor.session.getTextRange(origRange);
		if (!isItalic) {
			editor.session.replace(origRange, '*' + origText + '*');
		} else {
			editor.session.replace(editor.getSelectionRange(), origText);
		}
		editor.selection.setSelectionRange(origRange);
		editor.selection.shiftSelection(!isItalic ? 1 : -1);
		editor.focus();
	}
}

function insertMacro(insertText) {
	if (insertText.indexOf('[') < 0) {
		insertText = insertText + '[]';
	}
	var selectionStart = insertText.indexOf('[');
	var selectionEnd = insertText.indexOf(']') - 1;
	insertText = insertText.replace(/\[/g, '').replace(/\]/g, '');
	var len = insertText.length;
	var editor = $('#editor').data('editor');
	var range = editor.getSelectionRange();
	var selectedText = editor.session.getTextRange(range);
	if (selectedText === '') {
		selectedText = insertText.substring(selectionStart, selectionEnd);
	}
	var newText = insertText.substring(0, selectionStart) + selectedText + insertText.substring(selectionEnd);
	editor.session.replace(editor.getSelectionRange(), newText);
	editor.selection.setSelectionRange(range);
	editor.selection.shiftSelection(selectionStart);
	if (editor.selection.isEmpty()) {
		for (var i = 0; i < selectedText.length; i++) {
			editor.selection.selectRight();
		}
	}
	editor.focus();
}

function toggleFullscreen() {
	$('#titleFieldset, #pathFieldset, #textLabel, #tagsFieldset, #viewRestrictionRoleFieldset').toggle();
	var editorWrapperEl = $('.editor-wrapper');
	var height = editorWrapperEl.outerHeight();
	editorWrapperEl.css('height', height < 500 ? '575px' : '450px');
	$('#editor').data('editor').resize();
}

function showMarkdownHelp() {
	window.open('<c:url value="/help/markdown"/>', 'documentrMarkdownHelp',
		'width=800, height=600, dependent=yes, location=no, menubar=no, resizable=yes, scrollbars=yes, status=no, toolbar=no');
}

function updateInsertLinkButton() {
	var attachment = $('#insert-link-dialog .nav-tabs li:eq(0)').hasClass('active');
	var page = $('#insert-link-dialog .nav-tabs li:eq(1)').hasClass('active');
	var staticPage = $('#insert-link-dialog .nav-tabs li:eq(2)').hasClass('active');
	var external = $('#insert-link-dialog .nav-tabs li:eq(3)').hasClass('active');
	var linkedAttachment = $('#insert-link-dialog').data('linkedAttachment');
	var linkedPage = $('#insert-link-dialog').data('linkedPage');
	var linkedStaticPage = $('#insert-link-dialog').data('linkedStaticPage');
	var url = $('#insert-link-dialog input[name="externalLinkUrl"]').val();
	var valid = (attachment && documentr.isSomething(linkedAttachment)) ||
		(page && documentr.isSomething(linkedPage)) ||
		(staticPage && documentr.isSomething(linkedStaticPage)) ||
		(external && (url.length > 0));
	$('#insert-link-button').setButtonDisabled(!valid);
}

function openInsertLinkDialog(selectInitial) {
	var editor = $('#editor').data('editor');
	var linkText = editor.session.getTextRange(editor.getSelectionRange());
	$('#insert-attachment-link-text, #insert-page-link-text, #insert-static-page-link-text, #insert-link-dialog input[name="externalLinkText"]')
		.val(linkText);
	$('#insert-link-dialog input[name="externalLinkUrl"]').val('');
	$('#insert-link-dialog .nav-tabs a:first').tab('show');

	function showDialog() {
		updateInsertLinkButton();
		$('#insert-link-dialog').showModal();
	}

	if (documentr.isSomething(selectInitial)) {
		$('#linked-attachment-tree').destroyPageTree();
	}

	if (!$('#linked-attachment-tree').isPageTree()) {
		var tree = documentr.createPageTree($('#linked-attachment-tree'), {
				start: {
					type: 'page',
					projectName: '<c:out value="${projectName}"/>',
					branchName: '<c:out value="${branchName}"/>',
					pagePath: '<c:out value="${path}"/>'
				},
				checkBranchPermissions: 'VIEW',
				showPages: false,
				showAttachments: true
			})
			.bind('select_node.jstree', function(event, data) {
				var node = data.rslt.obj;
				var linkedAttachment = {
					path: node.data('name')
				};
				$('#insert-link-dialog').data('linkedAttachment', linkedAttachment);
				updateInsertLinkButton();
			})
			.bind('deselect_node.jstree', function() {
				$('#insert-link-dialog').data('linkedAttachment', null);
				updateInsertLinkButton();
			});
		if (documentr.isSomething(selectInitial)) {
			tree.bind('loaded.jstree', function() {
				tree.selectAttachment(selectInitial);
			});
		}

		documentr.createPageTree($('#linked-page-tree'), {
				start: {
					type: 'branch',
					projectName: '<c:out value="${projectName}"/>',
					branchName: '<c:out value="${branchName}"/>'
				},
				checkBranchPermissions: 'VIEW'
			})
			.bind('select_node.jstree', function(event, data) {
				var node = data.rslt.obj;
				var linkedPage = {
					path: node.data('path').replace(/\//g, ',')
				};
				$('#insert-link-dialog').data('linkedPage', linkedPage);
				updateInsertLinkButton();
			})
			.bind('deselect_node.jstree', function() {
				$('#insert-link-dialog').data('linkedPage', null);
				updateInsertLinkButton();
			});

		documentr.createPageTree($('#linked-static-page-tree'), {
				start: {
					type: 'application'
				},
				selectable: {
					projects: false,
					branches: false
				},
				checkBranchPermissions: 'VIEW'
			})
			.bind('select_node.jstree', function(event, data) {
				var node = data.rslt.obj;
				var linkedStaticPage = {
					projectName: node.data('projectName'),
					branchName: node.data('branchName'),
					path: node.data('path')
				};
				$('#insert-link-dialog').data('linkedStaticPage', linkedStaticPage);
				updateInsertLinkButton();
			})
			.bind('deselect_node.jstree', function() {
				$('#insert-link-dialog').data('linkedStaticPage', null);
				updateInsertLinkButton();
			});
		
		window.setTimeout(showDialog, 500);
	} else {
		showDialog();
	}
}

function insertLink() {
	$('#insert-link-dialog').hideModal();

	var attachment = $('#insert-link-dialog .nav-tabs li:eq(0)').hasClass('active');
	var page = $('#insert-link-dialog .nav-tabs li:eq(1)').hasClass('active');
	var staticPage = $('#insert-link-dialog .nav-tabs li:eq(2)').hasClass('active');
	var external = $('#insert-link-dialog .nav-tabs li:eq(3)').hasClass('active');
	var linkedAttachment = $('#insert-link-dialog').data('linkedAttachment');
	var linkedPage = $('#insert-link-dialog').data('linkedPage');
	var linkedStaticPage = $('#insert-link-dialog').data('linkedStaticPage');
	var url = $('#insert-link-dialog input[name="externalLinkUrl"]').val();

	var linkText;
	if (attachment) {
		linkText = $('#insert-attachment-link-text').val();
	} else if (page) {
		linkText = $('#insert-page-link-text').val();
	} else if (staticPage) {
		linkText = $('#insert-static-page-link-text').val();
	} else {
		linkText = $('#insert-link-dialog input[name="externalLinkText"]').val();
	}
	var link;
	if (attachment) {
		link = '=' + linkedAttachment.path;
	} else if (page) {
		link = linkedPage.path;
	} else if (staticPage) {
		link = '<c:url value="/page/"/>' + linkedStaticPage.projectName + '/' + linkedStaticPage.branchName + '/' +
			linkedStaticPage.path.replace(/\//g, ',');
	} else {
		link = url;
	}

	var text = '[[' + link + ' ' + linkText + ']]';
	var editor = $('#editor').data('editor');
	editor.session.replace(editor.getSelectionRange(), text);
	editor.selection.clearSelection();
	for (var i = 0; i < (text.length - link.length - 3); i++) {
		editor.selection.moveCursorLeft();
	}
	for (var i = 0; i < linkText.length; i++) {
		editor.selection.selectRight();
	}
	editor.focus();
}

function updateInsertImageButton() {
	var linkedImage = $('#insert-image-dialog').data('linkedImage');
	var altText = $('#insert-image-alttext').val();
	$('#insert-image-button').setButtonDisabled(!documentr.isSomething(linkedImage) || (altText.length === 0));
}

function openInsertImageDialog(selectInitial) {
	function showDialog() {
		$('#insert-image-alttext').val('');
		updateInsertImageButton();
		$('#insert-image-dialog').showModal();
	}

	var treeEl = $('#linked-image-tree');

	if (documentr.isSomething(selectInitial)) {
		treeEl.destroyPageTree();
	}

	if (!treeEl.isPageTree()) {
		documentr.createPageTree(treeEl, {
				start: {
					type: 'page',
					projectName: '<c:out value="${projectName}"/>',
					branchName: '<c:out value="${branchName}"/>',
					pagePath: '<c:out value="${path}"/>'
				},
				checkBranchPermissions: 'VIEW',
				showPages: false,
				showAttachments: true
			})
			.bind('select_node.jstree', function(event, data) {
				var node = data.rslt.obj;
				var linkedImage = node.data('name');
				$('#insert-image-dialog').data('linkedImage', linkedImage);
				updateInsertImageButton();
			})
			.bind('deselect_node.jstree', function() {
				$('#insert-image-dialog').data('linkedImage', null);
				updateInsertImageButton();
			});
		if (documentr.isSomething(selectInitial)) {
			treeEl.bind('loaded.jstree', function() {
				treeEl.selectAttachment(selectInitial);
			});
		}

		window.setTimeout(showDialog, 500);
	} else {
		showDialog();
	}
}

function insertImage() {
	$('#insert-image-dialog').hideModal();
	
	var linkedImage = $('#insert-image-dialog').data('linkedImage');
	var altText = $('#insert-image-alttext').val();
	var thumbnail = $('#insert-image-thumbnail:checked').length === 1;
	if (thumbnail) {
		linkedImage = linkedImage + ' | thumb';
	}

	var text = '![' + altText + '](' + linkedImage + ')';
	var editor = $('#editor').data('editor');
	editor.selection.clearSelection();
	editor.insert(text);
	for (var i = 0; i < (text.length - 2); i++) {
		editor.selection.moveCursorLeft();
	}
	for (var i = 0; i < altText.length; i++) {
		editor.selection.selectRight();
	}
	editor.focus();
}

function addTag(tag) {
	var tags = [];
	$('#pageForm input[name="tags"]').each(function() {
		var tag = $(this).val();
		tags.push(tag);
	});
	
	if ($.inArray(tag, tags) < 0) {
		$('#pageForm input[name="tags"]').remove();
	
		tags.push(tag);
		
		var formEl = $('#pageForm');
		$(tags).each(function() {
			var tag = this;
			var el = $('<input type="hidden" name="tags"/>');
			formEl.append(el);
			el.val(tag);
		});
		
		updateTags();
	}
}

function updateTags() {
	var tags = [];
	$('#pageForm input[name="tags"]').each(function() {
		var tag = $(this).val();
		tags.push(tag);
	});

	tags.sort();

	$('#tagsContainer .page-tag, #tagsContainer br').remove();
	var inputEl = $('#newTagInput');
	$(tags).each(function() {
		var tag = this;
		var tagEl = $('<span class="page-tag"/>');
		tagEl.attr('data-tag', tag);
		tagEl.text(tag);
		var closeEl = $('<span class="close">&#x00D7</span>');
		tagEl.append(closeEl);
		inputEl.before(tagEl);
	});
	
	if (tags.length > 0) {
		inputEl.before($('<br/>'));
		hookTags();
	}
}

function deleteTag(tagToDelete) {
	$('#pageForm input[name="tags"]').each(function() {
		var el = $(this);
		var tag = el.val();
		if (tag === tagToDelete) {
			el.remove();
		}
	});
	updateTags();
	dirty = true;
}

function hookTags() {
	$('#tagsContainer .page-tag').each(function() {
		var tagEl = $(this);
		var tag = tagEl.attr('data-tag');
		tagEl.off('click', '.close');
		tagEl.on('click', '.close', {
			tag: tag
		}, function(e) {
			deleteTag(e.data.tag);
		});
	});
}

function prepareForm() {
	var text = $('#editor').data('editor').getValue();
	$('#pageForm input[name="text"]').val(text);
	dirty = false;
	return true;
}

function cancelUpload() {
	$('#upload-dialog .modal-footer a').setButtonDisabled(true);
	$.each(jqXHRs, function(idx, jqXHR) {
		jqXHR.abort();
	});
	jqXHRs = [];
}

$(function() {
	<c:if test="${empty pageForm.path}">
		var el = $('#pageForm #title');
		el.blur(function() {
			var pinPathButton = $('#pinPathButton');
			if (!pinPathButton.hasClass('active')) {
				var fieldset = $('#pathFieldset');
				fieldset.removeClass('warning').removeClass('error');
				$('#pathExistsWarning').remove();
		
				var value = el.val();
				if (value.length > 0) {
					$.ajax({
						url: '<c:url value="/page/generateName/${pageForm.projectName}/${pageForm.branchName}/${d:toUrlPagePath(hierarchyPagePath)}/json"/>',
						type: 'POST',
						dataType: 'json',
						data: {
							title: value
						},
						success: function(result) {
							console.log(result);
							$('#pageForm #path').val(result.path);
							if (result.exists) {
								fieldset.addClass('warning');
								var controls = $('#pathControls');
								controls.append($('<span id="pathExistsWarning" class="help-inline">' +
									'<spring:message code="page.path.exists"/></span>'));
							}
							$('#pinPathButton').removeClass('disabled').removeClass('active');
						}
					});
				}
			}
		});
	</c:if>

	$('#insert-link-dialog .nav-tabs a').click(function(e) {
		e.preventDefault();
		$(this).tab('show');
		updateInsertLinkButton();
	});
	
	$('#newTagInput')
		.typeahead({
			source: allTags
		})
		.keydown(function(e) {
			var el = $('#newTagInput');
			var typeaheadShown = el.data('typeahead').shown;
			if (e.which == 13) {
				if (!typeaheadShown) {
					e.preventDefault();
		
					var newTag = $.trim(el.val());
					el.val('');
					if (newTag.length > 0) {
						addTag(newTag);
					}
				}
			} else if (!allTagsLoaded) {
				allTagsLoaded = true;
				$.ajax({
					url: '<c:url value="/search/tags/json"/>',
					type: 'GET',
					dataType: 'json',
					success: function(result) {
						$(result).each(function() {
							allTags.push(this);
						});
					}
				});
			}
		});
	
	hookTags();
	
	var editor = ace.edit('editor');
	$('#editor').data('editor', editor);
	editor.setTheme('ace/theme/chrome');
	editor.session.setMode('ace/mode/markdown');
	editor.setDisplayIndentGuides(true);
	editor.renderer.setShowGutter(false);
	editor.session.setUseWrapMode(true);
	editor.session.setWrapLimitRange(null, null);
	editor.renderer.setShowPrintMargin(false);
	editor.session.setUseSoftTabs(false);
	editor.setHighlightSelectedWord(false);

	$('input[type="file"]').fileupload({
		dropZone: $('#editor'),
		url: '<c:url value="/attachment/saveViaJson/${pageForm.projectName}/${pageForm.branchName}/${d:toUrlPagePath(hierarchyPagePath)}/json"/>',
		dataType: 'json',
		add: function(e, data) {
			var jqXHR = data.submit();
			jqXHRs.push(jqXHR);
		},
		progressall: function(e, data) {
			$('#upload-dialog').showModal();
			var percent = parseInt(data.loaded / data.total * 100, 10);
			$('#upload-dialog .progress .bar').css('width', percent + '%');
		},
		always: function() {
			$('#upload-dialog').hideModal();
			$('#upload-dialog .modal-footer a').setButtonDisabled(false);
			$('#upload-dialog .progress .bar').css('width', '0%');
		},
		done: function(e, data) {
			var file = data.files[0];
			if (file.type.indexOf('image/') === 0) {
				openInsertImageDialog(file.name);
			} else {
				openInsertLinkDialog(file.name);
			}
		}
	});

	$(window).bind('beforeunload', function() {
		if (dirty) {
			return '<spring:message code="confirmLeavePage"/>';
		}
	});
	
	$('#pageForm input, #pageForm select').on('keypress change select', function() {
		dirty = true;
	});
	editor.session.on('change', function() {
		dirty = true;
	});
});

</dt:headerJS>

<dt:breadcrumbs>
	<li><a href="<c:url value="/projects"/>"><spring:message code="title.projects"/></a> <span class="divider">/</span></li>
	<li><a href="<c:url value="/project/${pageForm.projectName}"/>"><c:out value="${pageForm.projectName}"/></a> <span class="divider">/</span></li>
	<li><a href="<c:url value="/page/${pageForm.projectName}/${pageForm.branchName}/home"/>"><c:out value="${pageForm.branchName}"/></a> <span class="divider">/</span></li>
	<c:set var="hierarchy" value="${d:getPagePathHierarchy(pageForm.projectName, pageForm.branchName, hierarchyPagePath)}"/>
	<c:forEach var="entry" items="${hierarchy}" varStatus="status">
		<c:if test="${!status.first}">
			<li><a href="<c:url value="/page/${pageForm.projectName}/${pageForm.branchName}/${d:toUrlPagePath(entry)}"/>"><c:out value="${d:getPageTitle(pageForm.projectName, pageForm.branchName, entry)}"/></a> <span class="divider">/</span></li>
		</c:if>
	</c:forEach>
	<li class="active"><spring:message code="title.editPage"/></li>
</dt:breadcrumbs>

<dt:pageTitle><spring:message code="title.editPage"/></dt:pageTitle>

<dt:page>

<div class="page-header"><h1><spring:message code="title.editPage"/></h1></div>

<p>
<c:set var="action"><c:url value="/page/save/${pageForm.projectName}/${pageForm.branchName}"/></c:set>
<form:form commandName="pageForm" action="${action}" method="POST" cssClass="well form-horizontal" onsubmit="prepareForm()">
	<c:set var="errorText"><form:errors cssClass="text-error"/></c:set>
	<c:if test="${mergeConflict}">
		<p class="text-error"><spring:message code="conflictExists"/></p>
	</c:if>

	<fieldset>
		<form:hidden path="parentPagePath"/>
		<form:hidden path="commit"/>
		<c:forEach var="tag" items="${pageForm.tags}">
			<input type="hidden" name="tags" value="<c:out value="${tag}"/>"/>
		</c:forEach>
		<form:hidden path="parentPageSplitRangeStart"/>
		<form:hidden path="parentPageSplitRangeEnd"/>
		<input type="hidden" name="text"/>
		
		<c:set var="errorText"><form:errors path="title"/></c:set>
		<div id="titleFieldset" class="control-group <c:if test="${!empty errorText}">error</c:if>">
			<form:label path="title" cssClass="control-label"><spring:message code="label.title"/>:</form:label>
			<div class="controls">
				<form:input path="title" cssClass="input-xlarge"/>
				<c:if test="${!empty errorText}"><span class="help-inline"><c:out value="${errorText}" escapeXml="false"/></span></c:if>
			</div>
		</div>
		<div id="pathFieldset" class="control-group">
			<form:hidden path="path"/>
			<form:label path="path" cssClass="control-label"><spring:message code="label.pathGeneratedAutomatically"/>:</form:label>
			<div class="controls" id="pathControls">
				<form:input path="path" cssClass="input-xlarge disabled" disabled="true"/>
				<c:if test="${empty pageForm.path}">
					<a id="pinPathButton" class="btn disabled" data-toggle="button" href="javascript:;" title="<spring:message code="button.pinPath"/>"><i class="icon-lock"></i></a>
				</c:if>
			</div>
		</div>
		<div class="control-group">
			<form:label id="textLabel" path="text" cssClass="control-label"><spring:message code="label.contents"/>:</form:label>
			<div id="textEditor" class="texteditor">
				<div id="textEditorToolbar" class="btn-toolbar btn-toolbar-icons">
					<div class="btn-group">
						<a id="togglePreviewButton" href="javascript:togglePreview();" class="btn" data-toggle="button" title="<spring:message code="button.showPreview"/>"><i class="icon-eye-open"></i></a>
						<a href="javascript:toggleFullscreen();" class="btn" data-toggle="button" title="<spring:message code="button.zoomEditor"/>"><i class="icon-fullscreen"></i></a>
					</div>
					<div class="btn-group">
						<a href="javascript:toggleStyleBold();" class="btn" title="<spring:message code="button.bold"/>"><i class="icon-bold"></i></a>
						<a href="javascript:toggleStyleItalic();" class="btn" title="<spring:message code="button.italic"/>"><i class="icon-italic"></i></a>
					</div>
					<div class="btn-group">
						<a href="javascript:openInsertImageDialog();" class="btn" title="<spring:message code="button.insertImage"/>"><i class="icon-picture"></i></a>
					</div>
					<div class="btn-group">
						<a href="javascript:openInsertLinkDialog();" class="btn" title="<spring:message code="button.insertLink"/>"><i class="icon-share-alt"></i></a>
					</div>
					<div class="btn-group">
						<a class="btn dropdown-toggle" data-toggle="dropdown" href="#"><spring:message code="button.macros"/> <span class="caret"></span></a>
						<ul class="dropdown-menu">
							<c:set var="macros" value="${d:getMacros()}"/>
							<c:forEach var="macro" items="${macros}">
								<dt:dropdownEntry>
									<li><a href="javascript:insertMacro('<c:out value="${d:escapeJavaScript(macro.insertText)}"/>');"><c:out value="${macro.title}"/> <div class="macro-description"><c:out value="${macro.description}"/></div></a></li>
								</dt:dropdownEntry>
							</c:forEach>
						</ul>
					</div>
					<div class="btn-group">
						<a href="javascript:showMarkdownHelp();" class="btn" title="<spring:message code="button.showFormattingHelp"/>"><i class="icon-question-sign"></i></a>
					</div>
				</div>
				<div class="editor-wrapper">
					<!--__NOTRIM__--><div id="editor"><c:out value="${pageForm.text}"/></div><!--__/NOTRIM__-->
				</div>
				<span class="help-block"><spring:message code="dragFilesOntoEditorToUpload"/></span>
			</div>
		</div>
		<div id="tagsFieldset" class="control-group">
			<label class="control-label"><spring:message code="label.tags"/>:</label>
			<div class="controls" id="tagsContainer">
				<c:if test="${!empty pageForm.tags}">
					<c:forEach var="tag" items="${pageForm.tags}"><%--
						--%><span class="page-tag" data-tag="<c:out value="${tag}"/>"><c:out value="${tag}"/><span class="close">&#x00D7</span></span><%--
					--%></c:forEach>
					<br/>
				</c:if>
				<input type="text" id="newTagInput" placeholder="<spring:message code="enterNewTag"/>" class="input-large" autocomplete="off"/>
			</div>
		</div>
		<div id="viewRestrictionRoleFieldset" class="control-group">
			<form:label path="viewRestrictionRole" cssClass="control-label"><spring:message code="label.visibleForRole"/>:</form:label>
			<div class="controls">
				<form:select path="viewRestrictionRole">
					<form:option value="">(<spring:message code="everyone"/>)</form:option>
					<c:set var="roles" value="${d:listRoles()}"/>
					<form:options items="${roles}"/>
				</form:select>
			</div>
		</div>
		<div class="form-actions">
			<input type="submit" class="btn btn-primary" value="<spring:message code="button.save"/>"/>
			<a href="<c:url value="/page/${pageForm.projectName}/${pageForm.branchName}/${d:toUrlPagePath(hierarchyPagePath)}"/>" class="btn"><spring:message code="button.cancel"/></a>
		</div>
	</fieldset>
</form:form>
</p>

<div class="modal" id="insert-link-dialog" style="display: none;">
	<div class="modal-header">
		<button class="close" onclick="$('#insert-link-dialog').hideModal();">&#x00D7</button>
		<h3><spring:message code="title.insertLink"/></h3>
	</div>
	<div class="modal-body">
		<form id="insertLinkForm" action="" method="POST" class="form-horizontal">
			<ul class="nav nav-tabs">
				<li class="active"><a href="#insert-attachment-link"><spring:message code="title.attachment"/></a></li>
				<li><a href="#insert-page-link"><spring:message code="title.page"/></a></li>
				<li><a href="#insert-static-page-link"><spring:message code="title.pageStatic"/></a></li>
				<li><a href="#insert-external-link"><spring:message code="title.externalWebPage"/></a></li>
			</ul>

			<div class="tab-content">
				<fieldset id="insert-attachment-link" class="tab-pane active">
					<div class="control-group">
						<label class="control-label"><spring:message code="label.linkedPage"/>:</label>
						<div class="controls">
							<div id="linked-attachment-tree"></div>
						</div>
					</div>
					<div class="control-group">
						<label class="control-label"><spring:message code="label.linkText"/>:</label>
						<div class="controls">
							<input type="text" id="insert-attachment-link-text" class="input-xlarge"/>
						</div>
					</div>
				</fieldset>

				<fieldset id="insert-page-link" class="tab-pane">
					<div class="control-group">
						<label class="control-label"><spring:message code="label.linkedPage"/>:</label>
						<div class="controls">
							<div id="linked-page-tree"></div>
						</div>
					</div>
					<div class="control-group">
						<label class="control-label"><spring:message code="label.linkText"/>:</label>
						<div class="controls">
							<input type="text" id="insert-page-link-text" class="input-xlarge"/>
						</div>
					</div>
				</fieldset>

				<fieldset id="insert-static-page-link" class="tab-pane">
					<div class="control-group">
						<label class="control-label"><spring:message code="label.linkedPage"/>:</label>
						<div class="controls">
							<div id="linked-static-page-tree"></div>
						</div>
					</div>
					<div class="control-group">
						<label class="control-label"><spring:message code="label.linkText"/>:</label>
						<div class="controls">
							<input type="text" id="insert-static-page-link-text" class="input-xlarge"/>
						</div>
					</div>
				</fieldset>

				<fieldset id="insert-external-link" class="tab-pane">
					<div class="control-group">
						<label class="control-label"><spring:message code="label.url"/>:</label>
						<div class="controls">
							<input type="text" name="externalLinkUrl" class="input-xlarge" onkeyup="updateInsertLinkButton()"/>
						</div>
					</div>
					<div class="control-group">
						<label class="control-label"><spring:message code="label.linkText"/>:</label>
						<div class="controls">
							<input type="text" name="externalLinkText" class="input-xlarge"/>
						</div>
					</div>
				</fieldset>
			</div>
		</form>
	</div>
	<div class="modal-footer">
		<a id="insert-link-button" href="javascript:void(insertLink());" class="btn btn-primary"><spring:message code="button.insertLink"/></a>
		<a href="javascript:void($('#insert-link-dialog').hideModal());" class="btn"><spring:message code="button.cancel"/></a>
	</div>
</div>

<div class="modal" id="insert-image-dialog" style="display: none;">
	<div class="modal-header">
		<button class="close" onclick="$('#insert-image-dialog').hideModal();">&#x00D7</button>
		<h3><spring:message code="title.insertImage"/></h3>
	</div>
	<div class="modal-body">
		<form action="" method="POST" class="form-horizontal">
			<fieldset>
				<div class="control-group">
					<label class="control-label"><spring:message code="label.image"/>:</label>
					<div class="controls">
						<div id="linked-image-tree"></div>
					</div>
				</div>
				<div class="control-group">
					<label class="control-label"><spring:message code="label.altText"/>:</label>
					<div class="controls">
						<input type="text" id="insert-image-alttext" class="input-xlarge" onkeyup="updateInsertImageButton()"/>
					</div>
				</div>
				<div class="control-group">
					<label class="checkbox">
						<input type="checkbox" id="insert-image-thumbnail">
						<spring:message code="label.insertAsThumbnail"/>
					</label>
				</div>
			</fieldset>
		</form>
	</div>
	<div class="modal-footer">
		<a id="insert-image-button" href="javascript:void(insertImage());" class="btn btn-primary"><spring:message code="button.insertImage"/></a>
		<a href="javascript:void($('#insert-image-dialog').hideModal());" class="btn"><spring:message code="button.cancel"/></a>
	</div>
</div>

<input type="file" name="file" style="display: none;"/>

<div class="modal" id="upload-dialog" style="display: none;">
	<div class="modal-header">
		<button class="close" onclick="cancelUpload();">&#x00D7</button>
		<h3><spring:message code="title.upload"/></h3>
	</div>
	<div class="modal-body">
		<div class="progress">
			<div class="bar"></div>
		</div>
	</div>
	<div class="modal-footer">
		<a href="javascript:void(cancelUpload());" class="btn"><spring:message code="button.cancel"/></a>
	</div>
</div>

</dt:page>

</sec:authorize>
