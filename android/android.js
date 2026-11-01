/* Version Android : le bouton PDF passe par le partage natif, destinataire pré-rempli. */
(function(){
  if(!window.Android) return;
  var btn=document.getElementById("pdfBtn"), msg=document.getElementById("pdfMsg");
  btn.onclick=function(){
    if(!window.jspdf){msg.textContent="Le module PDF n'a pas pu se charger.";return}
    var end=addDays(weekStart,6);
    var name="heures_"+key(weekStart)+".pdf";
    var b64=buildPdf().output("datauristring").split(",").pop();
    var sujet="Relevé d'heures"+(cfg.prenom?" - "+cfg.prenom:"")+" - semaine du "+weekStart.toLocaleDateString("fr-FR")+" au "+end.toLocaleDateString("fr-FR");
    var ok=Android.sharePdf(b64,name,cfg.dest||"",sujet);
    msg.textContent=ok?"Choisis Gmail : le destinataire "+(cfg.dest||"")+" est déjà rempli.":"Le PDF n'a pas pu être préparé.";
  };
})();
