let express = require('express');
let app = express();
let bodyParser = require('body-parser');

app.use( bodyParser.json() );
app.use( bodyParser.urlencoded({extended: true}) );

app.get('/', function(req,res){
    res.type('text/html; charset="utf-8"');
    res.send('mosi mosi');
});

let gps_data = [
    { uuid: '00001', lat: 23.0387, lon: 120.237 },
    { uuid: '00123', lat: 23.0378, lon: 120.255 }
];

app.get('/all', function(req,res){
    //res.type('text/html; charset="utf-8"');
    res.json( gps_data );
});

//假設使用者會POST上來
// {
//    uuid: "1234564897987",
//    lat: 12.1,
//    lon: 120.2
// }
app.post('/update', function(req,res){
    
    let _uuid = req.body.uuid;
    let _lat = parseFloat(req.body.lat);
    let _lon = parseFloat(req.body.lon);

    let isNew = true;
    gps_data.forEach(function(gps){
        if( gps.uuid==_uuid ){ //找到相同的uuid, 所以不是新的
            isNew = false;
            gps.lat = _lat;
            gps.lon = _lon;
        }
    });

    if( isNew ){ //新增一筆新的
        let newGPS = {
            uuid: _uuid,
            lat: _lat,
            lon: _lon
        }
        gps_data.push( newGPS );
    }

    res.json( gps_data );
});

app.listen(80);