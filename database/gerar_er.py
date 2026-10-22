#!/usr/bin/env python3
"""Gera o diagrama ER (PNG/SVG/DOT) lendo o catalogo do PostgreSQL. Requer: psql e Graphviz (dot).
Uso: PGPASSWORD=... python3 gerar_er.py [banco] [pasta_saida]"""
import subprocess, sys, os, html, collections
DB  = sys.argv[1] if len(sys.argv) > 1 else "stock4event"
OUT = sys.argv[2] if len(sys.argv) > 2 else "."
def q(sql):
    r = subprocess.run(["psql","-h",os.environ.get("PGHOST","localhost"),"-U",os.environ.get("PGUSER","postgres"),
                        "-d",DB,"-At","-F","\t","-c",sql],capture_output=True,text=True,check=True)
    return [l.split("\t") for l in r.stdout.splitlines() if l]
cols = q("""select c.relname, a.attname, format_type(a.atttypid,a.atttypmod), a.attnotnull
            from pg_attribute a join pg_class c on c.oid=a.attrelid join pg_namespace n on n.oid=c.relnamespace
            where n.nspname='public' and c.relkind='r' and a.attnum>0 and not a.attisdropped order by c.relname,a.attnum""")
pks = {(t,c) for t,c in q("""select c.relname,a.attname from pg_index i join pg_class c on c.oid=i.indrelid
        join pg_attribute a on a.attrelid=c.oid and a.attnum=any(i.indkey) where i.indisprimary""")}
uqs = {(t,c) for t,c in q("""select c.relname,a.attname from pg_index i join pg_class c on c.oid=i.indrelid
        join pg_attribute a on a.attrelid=c.oid and a.attnum=any(i.indkey)
        where i.indisunique and not i.indisprimary and i.indnatts=1""")}
fks = q("""select ch.relname, a.attname, pa.relname, a.attnotnull from pg_constraint k
        join pg_class ch on ch.oid=k.conrelid join pg_class pa on pa.oid=k.confrelid
        join pg_attribute a on a.attrelid=k.conrelid and a.attnum=k.conkey[1] where k.contype='f' order by 1,2""")
fkcols = {(t,c) for t,c,_,_ in fks}
grupo = {  # cor por dominio
 "#DCE9FB":["empresas","usuarios","perfis"],
 "#E2F3DC":["produtos","estoque_barraca","barracas","eventos"],
 "#FDEBD0":["pedido","itens_pedido","pagamentos","solicitacoes_compra","itens_solicitacao_compra"],
 "#F4DDF0":["caminhoneiros","entregas_logistica"],
 "#FFF3C4":["enderecos_entrega","cartoes_pagamento_salvos","chaves_pix_salvas","formas_pagamento_salvas"]}
cor = {t:c for c,ts in grupo.items() for t in ts}
tabs = collections.OrderedDict()
for t,c,ty,nn in cols: tabs.setdefault(t,[]).append((c,ty,nn=="t"))
L = ['digraph ER {','graph [rankdir=LR, splines=true, nodesep=0.35, ranksep=1.3, fontname="Helvetica", pad=0.4, '
     'label="Stock4event - Diagrama ER (PostgreSQL)\\nLinha cheia = FK obrigatoria   |   Linha tracejada = FK opcional   |   PK=chave primaria  FK=chave estrangeira  UQ=unica   |   ? = aceita NULL", labelloc=t, fontsize=18];',
     'node [shape=plain, fontname="Helvetica", fontsize=11]; edge [color="#555555", arrowsize=0.9];']
for t,cs in tabs.items():
    rows = [f'<TR><TD COLSPAN="3" BGCOLOR="#2F3B52"><FONT COLOR="white"><B>{html.escape(t)}</B></FONT></TD></TR>']
    for c,ty,nn in cs:
        tag = "PK" if (t,c) in pks else ("FK" if (t,c) in fkcols else ("UQ" if (t,c) in uqs else ""))
        if (t,c) in pks and (t,c) in fkcols: tag="PK,FK"
        nome = f"<B>{html.escape(c)}</B>" if tag else html.escape(c)
        nul = "" if nn else " ?"
        rows.append(f'<TR><TD ALIGN="LEFT" PORT="{c}">{nome}</TD><TD ALIGN="LEFT"><FONT COLOR="#666666">{html.escape(ty)}{nul}</FONT></TD><TD>{f'<FONT COLOR="#B03A2E">{tag}</FONT>' if tag else '&nbsp;'}</TD></TR>')
    L.append(f'"{t}" [label=<<TABLE BORDER="1" CELLBORDER="0" CELLSPACING="0" CELLPADDING="3" BGCOLOR="{cor.get(t,"#FFFFFF")}">{"".join(rows)}</TABLE>>];')
for filho,col,pai,nn in fks:
    estilo = "solid" if nn=="t" else "dashed"
    L.append(f'"{pai}" -> "{filho}":"{col}" [dir=both, arrowtail=tee, arrowhead=crow, style={estilo}];')
L.append('}')
os.makedirs(OUT,exist_ok=True)
dot=os.path.join(OUT,"stock4event_er.dot"); open(dot,"w",encoding="utf-8").write("\n".join(L))
for fmt,extra in (("svg",[]),("png",["-Gdpi=130"])):
    subprocess.run(["dot",f"-T{fmt}",*extra,dot,"-o",os.path.join(OUT,f"stock4event_er.{fmt}")],check=True)
print("Gerado:",", ".join(f"stock4event_er.{x}" for x in ("dot","svg","png")),"em",OUT)
