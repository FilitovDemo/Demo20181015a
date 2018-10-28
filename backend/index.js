let express = require('express');
let app = express();
let bodyParser = require('body-parser');

app.use( bodyParser.json() );
app.use( bodyParser.urlencoded({extended: true}) );

app.use(function(req, res, next) {
    res.header("Access-Control-Allow-Origin", "*");
    res.header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
    next();
});

app.get('/', function(req,res){
    res.type('text/html; charset="utf-8"')
        .send('後端範例');
});

let gps_data = [
    { uuid: '00001', lat: 23.0387, lon: 120.237, user: '測試內容1', time: 0 },
    { uuid: '00123', lat: 23.0378, lon: 120.255, user: '測試內容2', time: 0 }
];

//偵錯用
// http://URL/debug
app.get('/debug', function(req,res){
    res.status(200)
        .type('application/json; charset="utf-8"')
        .json( gps_data );
});

//假設使用者會POST上來
// {
//    uuid: "1234564897987",
//    lat: 12.1,
//    lon: 120.2,
//    user: "暱稱"
// }
//
// {uuid: "1234564897987",lat: 12.1,lon: 120.2,user: "暱稱"}
app.post('/update', function(req,res){
    console.log("UPDATE "+req.body);
    //確認參數完整
    if(!req.body.hasOwnProperty('uuid')
        || !req.body.hasOwnProperty('user')
        || !req.body.hasOwnProperty('lat')
        || !req.body.hasOwnProperty('lon')
        || req.body.uuid==""
        || isNaN(parseFloat(req.body.lat))
        || isNaN(parseFloat(req.body.lon))
    ) {
        res.status(400)
            .type('application/json; charset="utf-8"')
            .json('格式錯誤');
        return;
    }

    console.log("UPDATE "+JSON.stringify(req.body));
    let _uuid = req.body.uuid;
    let _lat = parseFloat(req.body.lat);
    let _lon = parseFloat(req.body.lon);
    let _user = req.body.user;

    //檢查經緯度
    if( _lat>90 || _lat<-90 || _lon>180 || _lon<-180 ){
        res.status(400)
            .type('application/json; charset="utf-8"')
            .json('格式錯誤');
        return;
    }
    
    //檢查是否為新使用者id, 順便準備回傳資料(捨棄uuid)
    //TODO: 刪去過時紀錄
    let isNew = true;
    let ret = [];
    let gps_data_new = [];
    let date_now = Date.now();
    gps_data.forEach(function(gps){
        //只留下存活週期中的部分
        if( gps.time>0 && (date_now - gps.time) > 120000 ){
            return;
        };

        if( gps.time>0 )
            gps.time = date_now;
        gps_data_new.push( gps );

        if( gps.uuid==_uuid ){ //找到相同的uuid, 所以不是新的
            isNew = false;
            gps.lat = _lat;
            gps.lon = _lon;
            gps.user = _user;

            //若是自己的資料，回送註明 uuid
        }

        //準備回傳資料(捨棄uuid)
        let rr = {
            uuid : '?',  //捨棄
            user : gps.user,
            lat  : gps.lat,
            lon  : gps.lon
        };
        if( gps.uuid==_uuid )
            rr.uuid = _uuid;
        ret.push( rr );

    });
    gps_data = gps_data_new;

    //第一次出現的 uuid
    if( isNew ){ //新增一筆新的
        let newGPS = {
            uuid: _uuid,
            lat: _lat,
            lon: _lon,
            user: _user,
            time: date_now
        }
        gps_data.push( newGPS );
        let rr = {
            uuid: _uuid,
            lat: _lat,
            lon: _lon,
            user: _user
        }
        ret.push( rr );
    }

    res.json( ret );
});

app.listen(18888);