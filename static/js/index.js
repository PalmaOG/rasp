var date_btn1 = document.getElementById('pn'); // Кнопка 1 курс
var group_block1 = document.getElementById('year1'); // Блок 1 курс

var date_btn2 = document.getElementById('vt'); // Кнопка 2 курс
var group_block2 = document.getElementById('year2'); // Блок 2 курс


var date_btn3 = document.getElementById('sr'); // Кнопка 3 курс
var group_block3 = document.getElementById('year3'); // Блок 3 курс

var date_btn4 = document.getElementById('cht'); // Кнопка 4 курс
var group_block4 = document.getElementById('year4'); // Блок 4 курс

var date_btn5 = document.getElementById('pt'); // Кнопка 5 курс
var group_block5 = document.getElementById('year5'); // Блок 5 курс

var date_btn6 = document.getElementById('sb'); // Кнопка 6 курс
var group_block6 = document.getElementById('year6'); // Блок 6 курс

var date_btn7 = document.getElementById('vs');

var sk = document.getElementById('imSkipped');


date_btn1.onclick = function() {     // 1 курс

    date_btn1.style.backgroundColor = "#cecece"
    date_btn2.style.backgroundColor = "white"
    date_btn3.style.backgroundColor = "white"
    date_btn4.style.backgroundColor = "white"
    date_btn5.style.backgroundColor = "white"
    date_btn6.style.backgroundColor = "white"
    date_btn7.style.backgroundColor = "white"
    
}

date_btn2.onclick = function() {     // 2 курс
    
    date_btn1.style.backgroundColor = "white"
    date_btn2.style.backgroundColor = "#cecece"
    date_btn3.style.backgroundColor = "white"
    date_btn4.style.backgroundColor = "white"
    date_btn5.style.backgroundColor = "white"
    date_btn6.style.backgroundColor = "white"
    date_btn7.style.backgroundColor = "white"

}

date_btn3.onclick = function() {     // 3 курс
    
    date_btn1.style.backgroundColor = "white"
    date_btn2.style.backgroundColor = "white"
    date_btn3.style.backgroundColor = "#cecece"
    date_btn4.style.backgroundColor = "white"
    date_btn5.style.backgroundColor = "white"
    date_btn6.style.backgroundColor = "white"
    date_btn7.style.backgroundColor = "white"

}

date_btn4.onclick = function() {     // 4 курс
    
    date_btn1.style.backgroundColor = "white"
    date_btn2.style.backgroundColor = "white"
    date_btn3.style.backgroundColor = "white"
    date_btn4.style.backgroundColor = "#cecece"
    date_btn5.style.backgroundColor = "white"
    date_btn6.style.backgroundColor = "white"
    date_btn7.style.backgroundColor = "white"

}

date_btn5.onclick = function() {     // 5 курс

    date_btn1.style.backgroundColor = "white"
    date_btn2.style.backgroundColor = "white"
    date_btn3.style.backgroundColor = "white"
    date_btn4.style.backgroundColor = "white"
    date_btn5.style.backgroundColor = "#cecece"
    date_btn6.style.backgroundColor = "white"
    date_btn7.style.backgroundColor = "white"

}

date_btn6.onclick = function() {     // 6 курс

    date_btn1.style.backgroundColor = "white"
    date_btn2.style.backgroundColor = "white"
    date_btn3.style.backgroundColor = "white"
    date_btn4.style.backgroundColor = "white"
    date_btn5.style.backgroundColor = "white"
    date_btn6.style.backgroundColor = "#cecece"
    date_btn7.style.backgroundColor = "white"

}

date_btn7.onclick = function() {

    date_btn1.style.backgroundColor = "white"
    date_btn2.style.backgroundColor = "white"
    date_btn3.style.backgroundColor = "white"
    date_btn4.style.backgroundColor = "white"
    date_btn5.style.backgroundColor = "white"
    date_btn6.style.backgroundColor = "white"
    date_btn7.style.backgroundColor = "#cecece"

}


const exampleModal = document.getElementById('exampleModal')

if (exampleModal) {
  exampleModal.addEventListener('show.bs.modal', event => {
    // Button that triggered the modal
    const button = event.relatedTarget
    // Extract info from data-bs-* attributes
    const recipient = button.getAttribute('data-bs-whatever')
    // If necessary, you could initiate an Ajax request here
    // and then do the updating in a callback.

    // Update the modal's content.
    const modalTitle = exampleModal.querySelector('.modal-title')
    const modalBodyInput = exampleModal.querySelector('.modal-body input')
 
    modalBodyInput.value = recipient;

  })
}

var f = document.getElementById('reasoning_docs');
f.onchange = function() {

    document.getElementById('reason-label').style.display = "none";
    document.getElementById('files-cancel').style.display = "inline-block";
    var mb = document.getElementById('f-names');
    mb.innerHTML = "<br>";
    for (var i = 0; i < this.files.length; i++) {
        
        t = '<span class="align-middle">' + this.files[i].name + "</span><br><br>";
        var tip = this.files[i].type;
        switch (tip) {
            case 'application/msword': // .doc
                mb.innerHTML += '<img src="static/img/word.png" width="30"> ' + t;
                break;
            case 'application/vnd.openxmlformats-officedocument.wordprocessingml.document':  // .docx
                mb.innerHTML += '<img src="static/img/word.png" width="30"> ' + t;
                break;
            case 'application/pdf': // .pdf
                mb.innerHTML += '<img src="static/img/pdf.png" width="30"> ' + t;
                break;
            case 'image/png': // .png
                mb.innerHTML += '<img src="static/img/img.png" width="30"> ' + t;
                break;
            case 'image/jpeg': // .jpeg
                mb.innerHTML += '<img src="static/img/img.png" width="30"> ' + t;
                break;
            default:
                alert("Неверный тип файла!");
                mb.innerHTML = "<br>";
                document.getElementById('reason-label').style.display = "inline-block"
                document.getElementById('files-cancel').style.display = "none";
                break;
        }
    }
}


var otm = document.getElementById('files-cancel');
otm.onclick = function()
{
    document.getElementById('reason-label').style.display = "inline-block"
    document.getElementById('f-names').innerHTML = "<br>";
    this.style.display = "none";
}