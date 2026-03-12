package com.vicpoo.shopy.di

import com.vicpoo.shopy.domain.repository.*
import com.vicpoo.shopy.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    // User UseCases
    @Provides
    @ViewModelScoped
    fun provideRegisterUseCase(repository: UserRepository): RegisterUseCase =
        RegisterUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideLoginUseCase(repository: UserRepository): LoginUseCase =
        LoginUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideLoginWithGoogleUseCase(repository: UserRepository): LoginWithGoogleUseCase =
        LoginWithGoogleUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideGetAllUsersUseCase(repository: UserRepository): GetAllUsersUseCase =
        GetAllUsersUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideGetUserByIdUseCase(repository: UserRepository): GetUserByIdUseCase =
        GetUserByIdUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideCreateUserUseCase(repository: UserRepository): CreateUserUseCase =
        CreateUserUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideUpdateUserUseCase(repository: UserRepository): UpdateUserUseCase =
        UpdateUserUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideDeleteUserUseCase(repository: UserRepository): DeleteUserUseCase =
        DeleteUserUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideGetCurrentUserUseCase(repository: UserRepository): GetCurrentUserUseCase =
        GetCurrentUserUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideLogoutUseCase(repository: UserRepository): LogoutUseCase =
        LogoutUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideChangeUserRoleUseCase(repository: UserRepository): ChangeUserRoleUseCase =
        ChangeUserRoleUseCase(repository)

    // Cloth UseCases
    @Provides
    @ViewModelScoped
    fun provideGetAllClothesUseCase(repository: ClothRepository): GetAllClothesUseCase =
        GetAllClothesUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideGetClothByIdUseCase(repository: ClothRepository): GetClothByIdUseCase =
        GetClothByIdUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideCreateClothUseCase(repository: ClothRepository): CreateClothUseCase =
        CreateClothUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideUpdateClothUseCase(repository: ClothRepository): UpdateClothUseCase =
        UpdateClothUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideDeleteClothUseCase(repository: ClothRepository): DeleteClothUseCase =
        DeleteClothUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideGetClothesBySellerUseCase(repository: ClothRepository): GetClothesBySellerUseCase =
        GetClothesBySellerUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideObserveClothesBySellerUseCase(repository: ClothRepository): ObserveClothesBySellerUseCase =
        ObserveClothesBySellerUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideSearchClothByNameUseCase(repository: ClothRepository): SearchClothByNameUseCase =
        SearchClothByNameUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideSearchClothBySizeUseCase(repository: ClothRepository): SearchClothBySizeUseCase =
        SearchClothBySizeUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideSearchClothByPriceRangeUseCase(repository: ClothRepository): SearchClothByPriceRangeUseCase =
        SearchClothByPriceRangeUseCase(repository)

    // Cart UseCases
    @Provides
    @ViewModelScoped
    fun provideGetCartItemsUseCase(repository: CartRepository): GetCartItemsUseCase =
        GetCartItemsUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideAddToCartUseCase(repository: CartRepository): AddToCartUseCase =
        AddToCartUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideRemoveFromCartUseCase(repository: CartRepository): RemoveFromCartUseCase =
        RemoveFromCartUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideUpdateCartQuantityUseCase(repository: CartRepository): UpdateCartQuantityUseCase =
        UpdateCartQuantityUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideClearCartUseCase(repository: CartRepository): ClearCartUseCase =
        ClearCartUseCase(repository)

    // Notification UseCases
    @Provides
    @ViewModelScoped
    fun provideCreateNotificationUseCase(repository: NotificationRepository): CreateNotificationUseCase =
        CreateNotificationUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideGetNotificationsUseCase(repository: NotificationRepository): GetNotificationsUseCase =
        GetNotificationsUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideMarkNotificationAsReadUseCase(repository: NotificationRepository): MarkNotificationAsReadUseCase =
        MarkNotificationAsReadUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideMarkAllNotificationsAsReadUseCase(repository: NotificationRepository): MarkAllNotificationsAsReadUseCase =
        MarkAllNotificationsAsReadUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideDeleteNotificationUseCase(repository: NotificationRepository): DeleteNotificationUseCase =
        DeleteNotificationUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideGetUnreadNotificationCountUseCase(repository: NotificationRepository): GetUnreadNotificationCountUseCase =
        GetUnreadNotificationCountUseCase(repository)
}