(function(){
const E=esc,LG=[["en","English"],["am","አማርኛ"],["ti","ትግርኛ"],["om","Afaan Oromoo"],["ar","العربية"],["fr","Français"]],MO=["😞","😕","😐","🙂","😄"];
const TR=`ዛሬ|ሎሚ|Har'a|Ngayon
ቆጣሪ|ቆጸራ|Tikaa sa'aatii|Timer
ፕሮግራም|መደብ|Sagantaa|Programa
ጥናት|ትምህርቲ|Qo'annoo|Pag-aaral
የዛሬ እቅድ|ናይ ሎሚ መደብ|Karoora har'aa|Plano ngayon
የዛሬ ግምገማ|ናይ ሎሚ ግምገማ|Madaallii har'aa|Pagsusuri ngayon
ዛሬ እንዴት ነበር?|ሎሚ ከመይ ነይሩ?|Har'a akkam ture?|Kumusta ang araw mo?
የትምህርት ዓይነቶች|ዓይነት ትምህርቲ|Gosoota barnootaa|Mga asignatura
ማቴሪያሎቼ|ናተ ማቴሪያላት|Meeshaalee koo|Aking mga materyales
ጀምር|ጀምር|Jalqabi|Simulan
ጨምር|ወስኽ|Dabali|Idagdag
አስቀምጥ|ኣቐምጥ|Olkaa'i|I-save
ተመለስ|ተመለስ|Deebi'i|Bumalik
ታሪክ|ታሪኽ|Seenaa|Kasaysayan
ደህንነት|ጥዕና|Nageenya fi fayyaa|Kapakanan
ወደፊት|ንመጻኢ|Gara fuulduraatti|Hinaharap
ቋንቋ|ቋንቃ|Afaan|Wika
ቅንብሮች|ቅጥዒታት|Qindaa'ina|Mga setting
ዕለታዊ ፕሮግራም|ዕለታዊ መደብ|Sagantaa guyyaa|Pang-araw-araw
የክፍል ፕሮግራም|ናይ ክፍሊ መደብ|Sagantaa kutaa|Programa ng klase
መጽሐፍ|መጽሓፍ|Kitaaba|Aklat
ትምህርቶች|ትምህርትታት|Barnoota|Mga asignatura
ሌላ…|ካልእ…|Kan biraa…|Iba pa…
ምናሌ|ሜኑ|Baafata|Menu
የዛሬ ርዕስ|ናይ ሎሚ ኣርእስቲ|Mata duree har'aa|Pamagat ngayon
ዛሬ እንዴት ነበር|ሎሚ ከመይ ነይሩ|Har'a akkam ture|Kumusta ang araw mo
ትምህርት ምረጥ|ትምህርቲ ምረጽ|Barnoota filadhu|Pumili ng asignatura
እንቅልፍ|ድቃስ|Hirribaa|Tulog
ውሃ|ማይ|Bishaan|Tubig
ስሜት|ስምዒት|Miira|Mood
ግቦቼ|ዕላማታተይ|Kaayyoowwan koo|Aking mga layunin
ለወደፊቴ ደብዳቤ|ደብዳቤ ንመጻኢይ|Xalayaa gara fuulduraa|Liham sa hinaharap`.split("\n").map(r=>r.split("|")),IX={ti:1,om:2,fil:3},MAP={};
TR.forEach(r=>{for(const l in IX)(MAP[l]=MAP[l]||{})[r[0]]=r[IX[l]]});
function tr(){const l=D.lang||"am";if(l=="am"||!MAP[l])return;const m=MAP[l],w=document.createTreeWalker(document.body,NodeFilter.SHOW_TEXT);let n;while(n=w.nextNode()){if(/^(TEXTAREA|SCRIPT|STYLE)$/.test(n.parentNode.nodeName))continue;const x=n.nodeValue.match(/^([^\p{L}\p{N}]*)(.*?)\s*$/su);if(x&&m[x[2]])n.nodeValue=x[1]+m[x[2]]}}
const st=document.createElement("style");st.textContent='select{appearance:none;-webkit-appearance:none;padding-right:42px!important;background-image:url("data:image/svg+xml,%3Csvg xmlns=\'http://www.w3.org/2000/svg\' width=\'24\' height=\'24\'%3E%3Cpath d=\'M3 6h18l-9 13z\' fill=\'%23E4572E\'/%3E%3C/svg%3E");background-repeat:no-repeat;background-position:right 12px center}.xg{display:grid;grid-template-columns:repeat(7,1fr);gap:4px}.xg10{display:grid;grid-template-columns:repeat(10,1fr);gap:4px}#xb{position:fixed;top:calc(env(safe-area-inset-top,0px) + 14px);left:14px;z-index:12;padding:4px 13px;background:rgba(255,255,255,.2);color:#fff;font-size:24px}header{padding-left:68px!important}';document.head.appendChild(st);
const b=document.createElement("button");b.id="xb";b.textContent="☰";b.onclick=()=>menu();document.body.appendChild(b);
const dk=k=>D.days[k]||(D.days[k]={tasks:[],rate:0,note:""});
let pp="day",cm=new Date(),sel=key(),M=0;
function langCard(){return '<div class="card"><div class="mute">🌐 ቋንቋ</div><select onchange="XA.lang(this.value)">'+LG.map(l=>'<option value="'+l[0]+'"'+((D.lang||"am")==l[0]?' selected':'')+'>'+l[1]+'</option>').join("")+'</select></div>'}
function menu(){M=!M;let e=$("xm");if(e)e.remove();if(!M)return;e=document.createElement("div");e.id="xm";e.style.cssText="position:fixed;inset:0;z-index:40;background:rgba(0,0,0,.5)";e.onclick=ev=>{if(ev.target==e)menu()};
 const I=[["today","🏠 ዛሬ"],["plan","📅 ፕሮግራም"],["hist","🗄 ታሪክ"],["well","💚 ደህንነት"],["fut","🔮 ወደፊት"],["about","⚙ ቅንብሮች"]];
 e.innerHTML='<div style="width:82%;max-width:330px;height:100%;background:var(--bg);padding:calc(env(safe-area-inset-top,0px) + 16px) 14px;overflow:auto"><h2 style="margin:0 0 10px">☰ ምናሌ</h2>'+langCard()+I.map(x=>'<button class="sec" style="width:100%;margin:5px 0;text-align:left" onclick="XA.go(\''+x[0]+'\')">'+x[1]+'</button>').join("")+'<a href="https://gemini.google.com" style="display:block;margin:12px 0;color:var(--ac)">✨ Gemini</a><a href="https://drive.google.com" style="display:block;color:var(--ac)">☁ Google Drive</a></div>';
 document.body.appendChild(e);tr()}
function chips(a,c,f){return '<div class="chips">'+a.map(x=>'<button class="'+(c==x[0]?'on':'')+'" onclick="'+f+'(\''+x[0]+'\')">'+x[1]+'</button>').join("")+'</div>'}
function vPlan(){return chips([["day","📅 ዕለታዊ ፕሮግራም"],["cls","🎓 የክፍል ፕሮግራም"],["bk","📖 መጽሐፍ"],["sub","⚙ ትምህርቶች"]],pp,"XA.pp")+({day:vDay,cls:vCls,bk:vBk,sub:viewPlan}[pp])()}
function vDay(){const y=cm.getFullYear(),m=cm.getMonth(),f=new Date(y,m,1).getDay(),n=new Date(y,m+1,0).getDate(),td=key();let c="";for(let i=0;i<f;i++)c+="<span></span>";
 for(let d=1;d<=n;d++){const k=key(new Date(y,m,d)),has=D.days[k]&&D.days[k].tasks.length;c+='<button onclick="XA.pick(\''+k+'\')" style="padding:9px 0;font-size:16px;position:relative;'+(k==sel?'background:var(--gold);color:var(--ink)':'background:var(--card);color:var(--tx)')+(k==td?';border:3px solid var(--ac)':'')+'">'+d+(has?'<i style="position:absolute;bottom:3px;left:50%;width:6px;height:6px;margin-left:-3px;border-radius:3px;background:var(--bad)"></i>':'')+'</button>'}
 const o=D.days[sel]||{tasks:[]};
 let h='<div class="card"><div class="row" style="margin-bottom:8px"><button class="sec" onclick="XA.cal(-1)">‹</button><b style="text-align:center;font-size:19px;font-weight:900">'+es(new Date(y,m,15)).split(" ")[0]+' · '+(m+1)+'/'+y+'</b><button class="sec" onclick="XA.cal(1)">›</button></div><div class="xg">'+c+'</div></div>';
 h+='<div class="card"><b style="font-size:21px;font-weight:900">'+es(pd(sel))+'</b> '+(sel==td?'<span class="tag">ዛሬ</span>':'')+'<input placeholder="የዕለቱ ርዕስ" value="'+E(o.title||"")+'" onchange="XA.title(this.value)">';
 h+=o.tasks.map((x,i)=>'<div class="task'+(x.d?' done':'')+'"><input type="checkbox" class="chk" '+(x.d?'checked':'')+' onchange="XA.tg('+i+')"><span style="flex:1">'+(x.s?'<em class="tag">'+E(x.s)+'</em>':'')+E(x.t)+'</span><button class="x" onclick="XA.del('+i+')">✕</button></div>').join("")||'<div class="empty">ለዚህ ቀን ምንም የለም።</div>';
 h+='<select id="xs">'+subOpts("","ትምህርት ምረጥ")+'</select><div class="row"><input id="xi" placeholder="ተግባር ጻፍ"><button onclick="XA.add()">＋</button></div>';
 if(sel!=td&&o.tasks.length)h+='<button class="sec" style="width:100%;margin-top:8px" onclick="XA.toToday()">📥 ወደ «ዛሬ» ገልብጥ</button>';
 return h+'</div>'}
function vCls(){let h='<div class="card"><input id="cn" placeholder="የፕሮግራሙ ስም (ለምሳሌ፦ የ100 ቀን ፕሮግራም)"><div class="row"><input id="cd" type="number" value="100" placeholder="ቀናት"><input id="cst" type="date" value="'+key()+'"></div><button style="width:100%;margin-top:6px" onclick="XA.addC()">ጨምር</button></div>';
 return h+(D.cls=D.cls||[]).map((c,i)=>{const p=Math.round(c.d.length/c.n*100),t=Math.floor((pd(key())-pd(c.s))/864e5)+1;
  return '<div class="card"><div class="row"><div><b style="font-size:21px;font-weight:900">'+E(c.t)+'</b><div class="mute">'+c.d.length+' / '+c.n+' ቀን ('+p+'%)'+(t>=1&&t<=c.n?' · ዛሬ ቀን '+t:'')+'</div></div><button class="x" onclick="XA.delC('+i+')">✕</button></div><div class="bar"><i style="width:'+p+'%"></i></div><div class="xg10">'+Array.from({length:c.n},(_,j)=>'<button onclick="XA.cd('+i+','+j+')" style="padding:6px 0;font-size:12px;border-radius:8px;'+(c.d.includes(j)?'background:var(--ok);color:#fff':'background:var(--card);color:var(--tx);border:2px solid '+(j+1==t?'var(--gold)':'rgba(88,113,120,.3)'))+'">'+(j+1)+'</button>').join("")+'</div></div>'}).join("")}
function vBk(){let h='<div class="card"><input id="bt" placeholder="የመጽሐፍ ስም"><input id="bp" type="number" placeholder="ጠቅላላ ገጾች"><button style="width:100%;margin-top:6px" onclick="XA.addB()">＋ መጽሐፍ ጨምር</button></div>';
 return h+(D.books=D.books||[]).map((c,i)=>{const p=Math.min(100,Math.round(c.r/c.p*100));return '<div class="card"><div class="row"><div><b style="font-size:21px;font-weight:900">📖 '+E(c.t)+'</b><div class="mute">'+c.r+' / '+c.p+' ገጽ ('+p+'%)</div></div><button class="x" onclick="XA.delB('+i+')">✕</button></div><div class="bar"><i style="width:'+p+'%"></i></div><div class="row"><button onclick="XA.bp('+i+',1)">＋1</button><button onclick="XA.bp('+i+',5)">＋5</button><button onclick="XA.bp('+i+',10)">＋10</button><button class="sec" onclick="XA.bp('+i+',-1)">−1</button></div></div>'}).join("")}
function vHist(){const ks=[...new Set(Object.keys(D.days).concat(Object.keys(D.log)))].sort().reverse();let h='<h2>🗄 ታሪክ</h2>';if(!ks.length)return h+'<div class="card empty">ገና ታሪክ የለም።</div>';
 return h+ks.map(k=>{const d=D.days[k]||{tasks:[]},T=d.tasks||[],wb=d.wb||{};return '<details class="card"><summary>'+es(pd(k))+' · '+k.split("-").reverse().join("/")+'<br><span class="mute">'+(d.title?E(d.title)+' · ':'')+T.filter(x=>x.d).length+'/'+T.length+' ተግባር · '+fmt(dayMin(k))+(d.rate?' · '+"★".repeat(d.rate):'')+(wb.m?' · '+MO[wb.m-1]:'')+'</span></summary>'+T.map(x=>'<div>'+(x.d?'✅':'⬜')+' '+E(x.t)+'</div>').join("")+(d.note?'<div style="white-space:pre-wrap;margin-top:8px">📝 '+E(d.note)+'</div>':'')+'</details>'}).join("")}
function vWell(){const o=dk(key()),w=o.wb=o.wb||{m:0,sl:0,w:0};let h='<h2>💚 ደህንነት</h2><div class="card"><div>ስሜት</div><div class="row" style="margin:8px 0">'+MO.map((e,i)=>'<button style="font-size:26px;background:'+(w.m==i+1?'var(--gold)':'var(--card)')+'" onclick="XA.wb(\'m\','+(i+1)+')">'+e+'</button>').join("")+'</div><div>እንቅልፍ</div><select onchange="XA.wb(\'sl\',+this.value)">'+[0,3,4,5,6,7,8,9,10].map(x=>'<option value="'+x+'"'+(w.sl==x?' selected':'')+'>'+(x?x+' ሰዓት':'—')+'</option>').join("")+'</select><div style="margin-top:8px">ውሃ · '+w.w+' 🥛</div><div class="row"><button onclick="XA.wb(\'w\','+(w.w+1)+')">＋ ብርጭቆ</button><button class="sec" onclick="XA.wb(\'w\','+Math.max(0,w.w-1)+')">−</button></div></div><h2>7 ቀን</h2><div class="card row">';
 const d=new Date();for(let i=6;i>=0;i--){const x=new Date(d);x.setDate(d.getDate()-i);const q=(D.days[key(x)]||{}).wb;h+='<div style="text-align:center"><div style="font-size:26px">'+(q&&q.m?MO[q.m-1]:'·')+'</div><div class="mute">'+WD[x.getDay()].slice(0,2)+'</div></div>'}return h+'</div>'}
function vFut(){D.fut=D.fut||[];let h='<h2>🔮 ግቦቼ</h2><div class="card"><input id="ft" placeholder="ግብ (ለምሳሌ፦ ብሔራዊ ፈተና)"><input id="fd" type="date"><button style="width:100%;margin-top:6px" onclick="XA.addF()">ጨምር</button></div>';
 h+=D.fut.map((g,i)=>{const d=dl(g.d);return '<div class="card row"><div><b style="font-weight:900;font-size:20px">🎯 '+E(g.t)+'</b><div class="mute">'+(d===null?'':d<0?'ጊዜው አልፏል':d+' ቀን ቀርቷል')+'</div></div><button class="x" onclick="XA.delF('+i+')">✕</button></div>'}).join("");
 return h+'<h2>ለወደፊቴ ደብዳቤ</h2><div class="card"><textarea placeholder="ከ1 ዓመት በኋላ ለራስህ ምን ትላለህ?" onchange="D.letter=this.value;save()">'+E(D.letter||"")+'</textarea></div>'}
window.XA={
 lang(v){D.lang=v;save();if(M)menu();render()},
 go(t){if(M)menu();if(t=="today"||t=="plan"||t=="about")go(t);else{tab=t;document.querySelectorAll("nav button").forEach(x=>x.classList.remove("on"));render();scrollTo(0,0)}},
 pp(v){pp=v;render()},cal(n){cm=new Date(cm.getFullYear(),cm.getMonth()+n,1);render()},pick(k){sel=k;render()},
 title(v){dk(sel).title=v.trim();save()},
 add(){const v=$("xi").value.trim();if(!v)return;const s=$("xs").value;dk(sel).tasks.push({t:v,d:0,s:s&&s!="__o"?s:""});save();render()},
 tg(i){const x=dk(sel).tasks[i];x.d=x.d?0:1;save();render()},del(i){dk(sel).tasks.splice(i,1);save();render()},
 toToday(){const t=today(),o=dk(sel);if(o.title&&!t.title)t.title=o.title;o.tasks.forEach(x=>t.tasks.push({t:x.t,d:0,s:x.s}));save();go("today")},
 addC(){const n=$("cn").value.trim(),d=Math.max(1,Math.min(400,+$("cd").value||100));if(!n)return;D.cls.push({t:n,n:d,s:$("cst").value||key(),d:[]});save();render()},
 delC(i){if(confirm("ይሰረዝ?")){D.cls.splice(i,1);save();render()}},
 cd(i,j){const a=D.cls[i].d,k=a.indexOf(j);k<0?a.push(j):a.splice(k,1);save();render()},
 addB(){const t=$("bt").value.trim(),p=+$("bp").value;if(!t||!p)return;D.books.push({t:t,p:p,r:0});save();render()},
 delB(i){if(confirm("ይሰረዝ?")){D.books.splice(i,1);save();render()}},
 bp(i,n){const c=D.books[i];c.r=Math.max(0,Math.min(c.p,c.r+n));if(n>0)addLog("ንባብ",n);save();render()},
 wb(k,v){dk(key()).wb[k]=v;save();render()},
 addF(){const t=$("ft").value.trim();if(!t)return;D.fut.push({t:t,d:$("fd").value});save();render()},delF(i){D.fut.splice(i,1);save();render()}};
const XV={plan:vPlan,hist:vHist,well:vWell,fut:vFut},R0=render;
render=function(){if(D.me.done&&XV[tab]){document.body.className="t-plan";head();$("app").innerHTML=XV[tab]();if(tab=="plan")document.querySelectorAll("nav button").forEach(x=>x.classList.toggle("on",x.dataset.t=="plan"))}else{R0();if(!D.me.done)$("app").insertAdjacentHTML("afterbegin",langCard());else if(tab=="today"){const t=D.days[key()];if(t&&t.title)$("app").insertAdjacentHTML("afterbegin",'<div class="card" style="border-left:10px solid var(--gold)"><div class="mute">የዛሬ ርዕስ</div><b style="font-size:22px;font-weight:900">'+E(t.title)+'</b></div>')}}
 document.querySelectorAll("#app select").forEach(s=>{if(s.dataset.o)return;const f=s.options[0]&&s.options[0].text;if(!(/ትምህርት ምረጥ/.test(f)||s.id=="ws"))return;s.dataset.o=1;let o=[...s.options].find(x=>x.text=="ሌላ");if(!o){o=new Option("","");s.add(o)}o.value="__o";o.text="✏ ሌላ…"});tr()};
document.addEventListener("change",e=>{const s=e.target;if(s.tagName!="SELECT"||s.value!="__o")return;const i=document.createElement("input");i.placeholder="ስም ይጻፉ";s.after(i);i.focus();let d=0;const ok=()=>{if(d)return;d=1;const v=i.value.trim();if(v){s.add(new Option(v,v),s.options[s.options.length-1]);s.value=v;s.dispatchEvent(new Event("change",{bubbles:true}))}else s.selectedIndex=0;i.remove()};i.onkeydown=ev=>{if(ev.key=="Enter")ok()};i.onblur=ok});
render();
})();
