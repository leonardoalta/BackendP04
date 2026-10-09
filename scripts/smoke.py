import json,urllib.request,urllib.error,uuid,os
base=os.environ.get('API_URL','http://localhost:8080')
def req(method,path,body=None,token=None):
 headers={'Content-Type':'application/json'}
 if token:headers['Authorization']='Bearer '+token
 r=urllib.request.Request(base+path,data=json.dumps(body).encode() if body else None,method=method,headers=headers)
 try:
  with urllib.request.urlopen(r) as response:return response.status,json.load(response)
 except urllib.error.HTTPError as e:return e.code,json.load(e)
checks=[]
def check(name,actual,expected):
 assert actual==expected,(name,actual,expected)
 checks.append({'check':name,'status':actual})
status,data=req('GET','/actuator/health');check('health',status,200);assert data['status']=='UP'
status,_=req('GET','/api/v1/reportes');check('unauthenticated',status,401)
user={'nombre':'Verificación local','correoElectronico':'smoke-'+uuid.uuid4().hex[:8]+'@example.test','password':'PruebaLocalSegura123!'}
status,auth=req('POST','/api/v1/auth/register',user);check('register',status,201);token=auth['accessToken']
status,_=req('POST','/api/v1/auth/login',{'correoElectronico':user['correoElectronico'],'password':user['password']});check('login',status,200)
status,_=req('GET','/api/v1/categorias',token=token);check('categories',status,200)
status,report=req('POST','/api/v1/reportes',{'categoriaId':1,'zonaId':1,'titulo':'Verificación ficticia','descripcion':'Reporte generado para comprobar endpoints locales','prioridad':'MEDIA'},token);check('create',status,201);rid=report['id']
status,_=req('GET',f'/api/v1/reportes/{rid}',token=token);check('detail',status,200)
status,_=req('POST',f'/api/v1/reportes/{rid}/evidencias',{'tipo':'IMAGEN','referenciaFicticia':'simulada://prueba','descripcion':'Sin archivo real'},token);check('evidence',status,201)
status,_=req('PATCH',f'/api/v1/reportes/{rid}/estado',{'estado':'EN_REVISION','comentario':'Sin permiso'},token);check('forbidden_transition',status,403)
status,api=req('GET','/v3/api-docs');check('openapi',status,200);assert '/api/v1/reportes' in api['paths'];assert '201' in api['paths']['/api/v1/reportes']['post']['responses']
print(json.dumps({'checks':checks,'note':'Se conservaron un usuario, reporte y evidencia ficticios en reportes_ciudadanos.'},ensure_ascii=False,indent=2))
