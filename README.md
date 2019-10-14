## なにこれ
国土地理院の浸水想定区域データ http://nlftp.mlit.go.jp/ksj/gml/datalist/KsjTmplt-A31.html のデータを解析し、MySQLのGeometry型としてデータを作成して検索できるようにするSQLファイルを作るツールです。

## なにができるの
MySQLで特定座標が近隣河川の氾濫によって水没する可能性、したときの浸水深、対象河川、当該浸水想定の情報などが取得できるようになる、みたいな使い方を想定しています。

## 使い方
上記URLからzipファイルをダウンロードし、sourcesディレクトリにぶちこみ、sbt runするとoutputsディレクトリにSQLが生成されます。生成されるSQLファイルを書き換えたい場合はソースコードのPolygonSQLGenerator、SurveySQLGeneratorを書き換えるといいでしょう。
