function insertUnitData(){
    // alert("123");
    document.getElementById('text-input').innerText = '123';
}


const mKey = [128, 214, 36, 111, 87, 74, 8, 33, 117, 48, 158];

//128%214%36%111%87%74%8%33%117%48%158
//128021403601110870740803301170480158

function convertData(inArray) {
    let kN = 0;
    let outArray = [];
    let b1, b2;
    for (let i = 0; i < inArray.length; i++) {
        b1 = inArray[i];
        b2 = mKey[kN];
        outArray.push(b1 ^ b2);
        kN++;
        if (kN >= mKey.length)
            kN = 0;
    }
    return outArray;
}

function checkIt() {
    let str = document.getElementById('in_text').value;
    let s = '';
    let arr = stringToByteArray(str);
    arr = convertData(arr);
    document.getElementById('out_text').value = byteArrayToString(arr);
}

function stringToByteArray(str) {
    let bytes = [];
    for (let i = 0; i < str.length; ++i) {
        let code = str.charCodeAt(i);
        bytes.push(code);
    }
    return bytes;
}

function byteArrayToString(arr) {
    return String.fromCharCode.apply(null, arr);
}