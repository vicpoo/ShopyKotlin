const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.sendProductNotification = functions.database
    .ref('/products/{productId}')
    .onCreate(async (snapshot, context) => {
        const product = snapshot.val();
        const productId = context.params.productId;

        // Obtener información del vendedor
        const sellerSnapshot = await admin.database()
            .ref(`/users/${product.sellerId}`).once('value');
        const seller = sellerSnapshot.val();

        // Crear mensaje de notificación
        const payload = {
            notification: {
                title: '¡Nuevo producto en Shopy!',
                body: `${seller?.name || 'Un vendedor'} ha agregado "${product.name}" a la tienda`,
                image: product.image || '',
                sound: 'notification_sound'
            },
            data: {
                type: 'new_product',
                product_id: productId,
                seller_id: product.sellerId,
                click_action: 'OPEN_PRODUCT_ACTIVITY'
            },
            android: {
                notification: {
                    icon: 'ic_notification',
                    color: '#FFFF2E92',
                    sound: 'notification_sound',
                    priority: 'high',
                    channelId: 'shopy_notifications'
                }
            }
        };

        try {
            // Enviar a todos los usuarios
            const response = await admin.messaging().sendToTopic('all_users', payload);
            console.log('Notificación enviada exitosamente:', response);
            return response;
        } catch (error) {
            console.error('Error al enviar notificación:', error);
            throw error;
        }
    });