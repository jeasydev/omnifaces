var OmniFaces=OmniFaces||{};
OmniFaces.FixViewState=function(){function r(e){var t=e.getElementsByTagName("update");for(var r=0;r<t.length;r++){var i=t[r];if(n.exec(i.getAttribute("id"))){return i.firstChild.nodeValue}}return null}function i(e){for(var n=0;n<e.elements.length;n++){if(e.elements[n].name==t){return true}}return false}function s(e,n){var r;try{r=document.createElement("<input name='"+t+"'>")}catch(i){r=document.createElement("input");r.setAttribute("name",t)}r.setAttribute("type","hidden");r.setAttribute("value",n);r.setAttribute("autocomplete","off");e.appendChild(r)}function o(e){for(var n=0;n<e.elements.length;n++){var r=e.elements[n];if(r.name==t){r.parentNode.removeChild(r)}}}var e={};var t="javax.faces.ViewState";var n=new RegExp("^([\\w]+:)?"+t.replace(/\./g,"\\.")+"(:[0-9]+)?$");e.apply=function(e){if(typeof e==="undefined"){return}var t=r(e);if(!t){return}for(var n=0;n<document.forms.length;n++){var u=document.forms[n];if(u.method=="post"){if(!i(u)){s(u,t)}}else{o(u)}}};return e}();if(typeof jsf!=="undefined"){jsf.ajax.addOnEvent(function(e){if(e.status=="success"){OmniFaces.FixViewState.apply(e.responseXML)}})}if(typeof jQuery!=="undefined"){jQuery(document).ajaxComplete(function(e,t,n){if(typeof t!=="undefined"){OmniFaces.FixViewState.apply(t.responseXML)}})}
