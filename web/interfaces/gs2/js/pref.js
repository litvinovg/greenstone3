function changePref(val){
  document.PrefForm.qfm.value = val;
  // display different options associated with different modes
  if(val=='1') {    
    for (var i=0; i<document.getElementsByTagName('tr').length;i++) {
      var id = document.getElementsByTagName('tr')[i].id;
      if(id.indexOf("text")!=-1){
        document.getElementById(id).style.display='none';
      } else if(id.indexOf("tf")!=-1) {
        document.getElementById(id).style.display='none';
      } else if(id.indexOf("adv")!=-1){
          document.getElementById(id).style.display='table-row';
      }      
    }
  } else if (val=='0') {
    for (var i=0; i<document.getElementsByTagName('tr').length;i++) {
      var id = document.getElementsByTagName('tr')[i].id;
      if(id.indexOf("text")!=-1) {
        document.getElementById(id).style.display='none';
      } else if(id.indexOf("adv")!=-1){
        document.getElementById(id).style.display='none';
      } else if(id.indexOf("tf")!=-1) {
        document.getElementById(id).style.display='table-row';
      }
    }
  } else {
    for (var i=0; i<document.getElementsByTagName('tr').length;i++) {
      var id = document.getElementsByTagName('tr')[i].id;
      if(id.indexOf("text")!=-1){
        document.getElementById(id).style.display='table-row';
      } else if(id.indexOf("tf")!=-1) {
        document.getElementById(id).style.display='table-row';
      } else if(id.indexOf("adv")!=-1){
        document.getElementById(id).style.display='none';
      }
    }
  }        
}

function checkForm(){       
  var qfm = "2";
  for (var i=0; i<document.getElementsByName('qfm').length; i++) {
  	if(document.getElementsByName('qfm')[i].checked == true){
  		qfm = document.getElementsByName('qfm')[i].value;
  	}	
  }    
  // this is actually the text query mode
  if(qfm=='2'){
    document.PrefForm.qt.value = '0';
  }
  else {
    document.PrefForm.qt.value = '1';
  }
  return true;
}
