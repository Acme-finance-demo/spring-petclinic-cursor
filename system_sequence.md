# Spring Petclinic システムシーケンス図

このドキュメントは、Spring Petclinicアプリケーションの主要なユースケースにおけるクラス間の呼び出し関係をシーケンス図で表現しています。

## 1. オーナー検索・表示のシーケンス

```mermaid
sequenceDiagram
    participant User as ユーザー
    participant OC as OwnerController
    participant OR as OwnerRepository
    participant O as Owner
    participant P as Pet
    participant V as Visit

    User->>OC: GET /owners?lastName=Smith
    OC->>OR: findByLastNameStartingWith("Smith", pageable)
    OR-->>OC: Page<Owner>
    
    alt 検索結果が1件の場合
        OC->>User: redirect:/owners/{id}
        User->>OC: GET /owners/{id}
        OC->>OR: findById(id)
        OR-->>OC: Optional<Owner>
        OC->>O: getPets()
        O-->>OC: List<Pet>
        loop ペットごと
            OC->>P: getVisits()
            P-->>OC: Collection<Visit>
        end
        OC-->>User: ownerDetails view
    else 複数件の場合
        OC-->>User: ownersList view
    else 0件の場合
        OC-->>User: findOwners view (エラー)
    end
```

## 2. 新しいオーナー作成のシーケンス

```mermaid
sequenceDiagram
    participant User as ユーザー
    participant OC as OwnerController
    participant O as Owner
    participant OR as OwnerRepository

    User->>OC: GET /owners/new
    OC->>O: new Owner()
    O-->>OC: Owner instance
    OC-->>User: createOrUpdateOwnerForm view

    User->>OC: POST /owners/new (Owner data)
    OC->>OC: validate Owner
    
    alt バリデーション成功
        OC->>OR: save(owner)
        OR-->>OC: saved Owner
        OC-->>User: redirect:/owners/{id}
    else バリデーション失敗
        OC-->>User: createOrUpdateOwnerForm view (エラー)
    end
```

## 3. ペット追加のシーケンス

```mermaid
sequenceDiagram
    participant User as ユーザー
    participant PC as PetController
    participant OR as OwnerRepository
    participant PTR as PetTypeRepository
    participant O as Owner
    participant P as Pet
    participant PT as PetType

    User->>PC: GET /owners/{ownerId}/pets/new
    PC->>OR: findById(ownerId)
    OR-->>PC: Optional<Owner>
    PC->>PTR: findPetTypes()
    PTR-->>PC: List<PetType>
    PC->>P: new Pet()
    P-->>PC: Pet instance
    PC->>O: addPet(pet)
    PC-->>User: createOrUpdatePetForm view

    User->>PC: POST /owners/{ownerId}/pets/new (Pet data)
    PC->>PC: validate Pet
    PC->>O: getPet(name, true) [重複チェック]
    O-->>PC: Pet or null
    
    alt バリデーション成功 & 重複なし
        PC->>O: addPet(pet)
        PC->>OR: save(owner)
        OR-->>PC: saved Owner
        PC-->>User: redirect:/owners/{ownerId}
    else バリデーション失敗 or 重複あり
        PC-->>User: createOrUpdatePetForm view (エラー)
    end
```

## 4. 診察予約のシーケンス

```mermaid
sequenceDiagram
    participant User as ユーザー
    participant VC as VisitController
    participant OR as OwnerRepository
    participant O as Owner
    participant P as Pet
    participant V as Visit

    User->>VC: GET /owners/{ownerId}/pets/{petId}/visits/new
    VC->>OR: findById(ownerId)
    OR-->>VC: Optional<Owner>
    VC->>O: getPet(petId)
    O-->>VC: Pet
    VC->>V: new Visit()
    V-->>VC: Visit instance
    VC->>P: addVisit(visit)
    VC-->>User: createOrUpdateVisitForm view

    User->>VC: POST /owners/{ownerId}/pets/{petId}/visits/new (Visit data)
    VC->>VC: validate Visit
    
    alt バリデーション成功
        VC->>O: addVisit(petId, visit)
        O->>O: getPet(petId)
        O->>P: addVisit(visit)
        VC->>OR: save(owner)
        OR-->>VC: saved Owner
        VC-->>User: redirect:/owners/{ownerId}
    else バリデーション失敗
        VC-->>User: createOrUpdateVisitForm view (エラー)
    end
```

## 5. 獣医師一覧表示のシーケンス

```mermaid
sequenceDiagram
    participant User as ユーザー
    participant VetC as VetController
    participant VR as VetRepository
    participant Vets as Vets
    participant Vet as Vet
    participant S as Specialty

    User->>VetC: GET /vets.html?page=1
    VetC->>VR: findAll(pageable)
    VR-->>VetC: Page<Vet>
    VetC->>Vets: new Vets()
    Vets-->>VetC: Vets instance
    VetC->>Vets: getVetList().addAll(vets)
    
    loop 各獣医師について
        VetC->>Vet: getSpecialties()
        Vet->>S: getName() [ソート用]
        S-->>Vet: specialty name
        Vet-->>VetC: List<Specialty>
    end
    
    VetC-->>User: vetList view

    alt JSON API呼び出し
        User->>VetC: GET /vets
        VetC->>VR: findAll()
        VR-->>VetC: Collection<Vet>
        VetC->>Vets: new Vets()
        VetC->>Vets: getVetList().addAll(vets)
        VetC-->>User: Vets (JSON)
    end
```

## 6. ホーム画面表示のシーケンス

```mermaid
sequenceDiagram
    participant User as ユーザー
    participant WC as WelcomeController

    User->>WC: GET /
    WC-->>User: welcome view
```

## アーキテクチャ概要

このSpring Petclinicアプリケーションは、以下の層構造を持っています：

- **Controller層**: HTTP リクエストを処理し、ビジネスロジックを呼び出す
- **Repository層**: データアクセスを担当（Spring Data JPA使用）
- **Model層**: ドメインオブジェクト（Entity）を定義
- **View層**: Thymeleafテンプレートでレンダリング

### 主要なクラス関係

- `Owner` ← 1:N → `Pet` ← 1:N → `Visit`
- `Pet` ← N:1 → `PetType`
- `Vet` ← N:N → `Specialty`

### 使用されているSpring機能

- Spring MVC (Controller, RequestMapping)
- Spring Data JPA (Repository)
- Bean Validation (JSR-303)
- Thymeleaf (View Template)
- Spring Boot (Auto Configuration)