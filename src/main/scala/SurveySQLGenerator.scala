class SurveySQLGenerator(table: String) {
  def genInit: String =
    s"""create table if not exists ${table} (
       |  id bigint not null primary key,
       |  contents text(65535) not null,
       |  code int not null,
       |  author varchar(255) not null,
       |  `date` date not null,
       |  number varchar(255) not null,
       |  target varchar(255) not null,
       |  rainfall varchar(255) not null,
       |  city varchar(255) not null,
       |  others text(65535) not null
       |) engine=InnoDB default charset=utf8mb4;""".stripMargin

  def gen(s: Survey): String = {
    val csv = s"(${s.id}, '${s.contents.mkString("\n")}', ${s.code}, '${s.author}', '${s.date.toString}', '${s.number}', '${s.target}', '${s.rainfall}', '${s.city}', '${s.others.mkString("\n")}')"
    s"insert ignore into ${table} (id, contents, code, author, `date`, number, target, rainfall, city, others) values ${csv};"
  }
}
