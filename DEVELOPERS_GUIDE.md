# 🔧 GUIA DE DESENVOLVIMENTO

## Estrutura de Código

### Padrões Utilizados

#### 1. MVVM (Model-View-ViewModel)

Cada feature segue o padrão MVVM:

```
Mesa/
├── Model (Mesa.kt)
├── View (MesasScreen.kt)
├── ViewModel (MesaViewModel.kt)
└── Repository (MesaRepository.kt)
```

#### 2. Repository Pattern

Todos os acessos ao Firebase passam por repositórios:

```kotlin
// ❌ Evitar
FirebaseFirestore.getInstance().collection("mesas").get()

// ✅ Fazer
mesaRepository.getAllMesas()
```

#### 3. Composable Functions

Todas as telas são composables:

```kotlin
@Composable
fun MinhaScreen(
    param1: String,
    onCallback: (resultado) -> Unit
) {
    // UI aqui
}
```

---

## Como Adicionar Novos Recursos

### Exemplo: Adicionar novo item de Menu

#### 1. Criar o Model

`data/models/MenuItem.kt`

```kotlin
data class MenuItem(
    @DocumentId
    val id: String = "",
    val nome: String = "",
    val valor: Double = 0.0
) : Serializable
```

#### 2. Criar o Repository

`data/repositories/MenuRepository.kt`

```kotlin
class MenuRepository {
    private val firestore = FirebaseFirestore.getInstance()
    
    suspend fun getMenuItems(): List<MenuItem> {
        return firestore.collection("menu")
            .get()
            .await()
            .toObjects(MenuItem::class.java)
    }
}
```

#### 3. Criar o ViewModel

`ui/viewmodels/MenuViewModel.kt`

```kotlin
class MenuViewModel : ViewModel() {
    private val repository = MenuRepository()
    
    private val _menuItems = MutableStateFlow<List<MenuItem>>(emptyList())
    val menuItems: StateFlow<List<MenuItem>> = _menuItems
    
    fun loadMenuItems() {
        viewModelScope.launch {
            val items = repository.getMenuItems()
            _menuItems.value = items
        }
    }
}
```

#### 4. Criar a Composable Screen

`ui/screens/MenuScreen.kt`

```kotlin
@Composable
fun MenuScreen(
    viewModel: MenuViewModel = viewModel(),
    onItemClick: (MenuItem) -> Unit
) {
    val menuItems by viewModel.menuItems.collectAsState()
    
    LazyColumn {
        items(menuItems) { item ->
            MenuItem(
                item = item,
                onClick = { onItemClick(item) }
            )
        }
    }
}
```

#### 5. Integrar na Navigation

`MainActivity.kt`

```kotlin
when (currentScreen) {
    Screen.Menu -> {
        MenuScreen(
            onItemClick = { item ->
                // Handle item click
            }
        )
    }
}
```

---

## Conectar com Firebase

### 1. Criar Collection

Firebase Console > Firestore > Criar Coleção

```
Collection: menu
Documento 1:
{
  id: "1",
  nome: "X-Burger",
  valor: 25.00
}
```

### 2. Atualizar Regras de Segurança

```javascript
match /menu/{document=**} {
  allow read: if request.auth != null;
}
```

### 3. Usar no ViewModel

Como mostrado acima.

---

## Estado e Reatividade

### StateFlow

Para dados que mudam frequentemente:

```kotlin
private val _pedidos = MutableStateFlow<List<Pedido>>(emptyList())
val pedidos: StateFlow<List<Pedido>> = _pedidos

fun adicionarPedido(pedido: Pedido) {
    _pedidos.value = _pedidos.value + pedido
}
```

### collectAsState

Para observar StateFlow em Composables:

```kotlin
val pedidos by viewModel.pedidos.collectAsState()
```

### Flow com listen

Para sincronização em tempo real:

```kotlin
fun getPedidosByMesa(mesaId: String): Flow<List<Pedido>> = flow {
    collection
        .whereEqualTo("mesaId", mesaId)
        .addSnapshotListener { snapshot, _ ->
            val pedidos = snapshot?.toObjects(Pedido::class.java) ?: emptyList()
            emit(pedidos)
        }
}
```

---

## Tratamento de Erros

### Try-Catch

```kotlin
suspend fun criarPedido(pedido: Pedido): Result<Pedido> = try {
    val doc = collection.add(pedido).await()
    Result.success(pedido.copy(id = doc.id))
} catch (e: Exception) {
    Result.failure(e)
}
```

### No ViewModel

```kotlin
fun criarPedido(pedido: Pedido) {
    viewModelScope.launch {
        val result = repository.criarPedido(pedido)
        result.onSuccess { 
            _pedidos.value = _pedidos.value + it
        }
        result.onFailure { error ->
            _error.value = error.message
        }
    }
}
```

---

## Logging

Use o LoggerUtil para logging centralizado:

```kotlin
import com.example.mesafacil.utils.LoggerUtil

LoggerUtil.d("Debug: Carregando pedidos")
LoggerUtil.e("Erro ao salvar:", exception)
LoggerUtil.i("Info: Pedido criado com sucesso")
```

---

## Testes Unitários

### Teste de Repository

`app/src/test/java/com/example/mesafacil/data/repositories/MesaRepositoryTest.kt`

```kotlin
class MesaRepositoryTest {
    private val repository = MesaRepository()
    
    @Test
    fun testGetAllMesas() = runBlocking {
        val result = repository.getAllMesas()
        assertNotNull(result)
    }
}
```

### Teste de ViewModel

```kotlin
class MesaViewModelTest {
    private val viewModel = MesaViewModel()
    
    @Test
    fun testLoadMesas() {
        viewModel.loadMesas()
        val mesas = viewModel.mesas.value
        assertNotNull(mesas)
    }
}
```

---

## Performance

### Otimizações

1. **Lazy Loading**
   - Carregue dados conforme necessário
   - Evite carregar tudo de uma vez

2. **Paginação**
   - Se houver muitos itens, use paginação
   - Carregue 20 itens por página

3. **Caching Local**
   - Firebase oferece caching automático
   - Habilite persistência:

```kotlin
val settings = FirebaseFirestoreSettings.Builder()
    .setPersistenceEnabled(true)
    .build()
firestore.firestoreSettings = settings
```

4. **Índices**
   - Crie índices para queries complexas
   - Firebase sugere automaticamente

---

## Segurança

### Regras de Firestore

Sempre valide acessos:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Somente usuários autenticados
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
    
    // Dados pessoais - somente o próprio usuário
    match /users/{uid} {
      allow read, write: if request.auth.uid == uid;
    }
  }
}
```

### Validação de Input

```kotlin
fun validarEmail(email: String): Boolean {
    return email.isNotEmpty() && 
           Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

fun validarSenha(senha: String): Boolean {
    return senha.length >= 6
}
```

---

## Build e Deploy

### Build APK

```bash
./gradlew assembleRelease
```

Arquivo: `app/build/outputs/apk/release/app-release.apk`

### Upload Google Play

1. Crie app no Google Play Console
2. Configure informações do app
3. Faça upload do APK assinado
4. Preencha formulários de consentimento
5. Submeta para análise
6. Publique

---

## Git Workflow

```bash
# Criar branch feature
git checkout -b feature/nova-funcionalidade

# Fazer commits
git add .
git commit -m "Adicionar nova funcionalidade"

# Push
git push origin feature/nova-funcionalidade

# Criar Pull Request
# GitHub > New Pull Request

# Merge para main
# após revisão e testes
```

---

## Checklist Antes de Submeter

- [ ] Código está testado
- [ ] Sem erros de compilação
- [ ] Sem warnings
- [ ] Documentação atualizada
- [ ] Commits descritivos
- [ ] Testes passam
- [ ] Performance verificada
- [ ] Sem dados sensíveis em código

---

**Desenvolvido para a comunidade de desenvolvedores Android!**
